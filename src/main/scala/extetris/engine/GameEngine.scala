package extetris.engine

import extetris.model.{GameState, GameInput, GameEvent, Position, GameStatus}
import extetris.pieces.{Piece, PieceDefinition, PieceRegistry, PieceId, StandardPieces, CustomPieces}
import extetris.board.Board
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig}
import extetris.generation.{PieceGenerator, RandomState}

object GameEngine:
  def step(state: GameState, input: GameInput): (GameState, List[GameEvent]) =
    input match
      case GameInput.MoveLeft => move(state, -1, 0)
      case GameInput.MoveRight => move(state, 1, 0)
      case GameInput.MoveDown => move(state, 0, 1)
      case GameInput.HardDrop => hardDrop(state)
      case GameInput.RotateCW => rotate(state, clockwise = true)
      case GameInput.RotateCCW => rotate(state, clockwise = false)
      case GameInput.Hold => hold(state)
      case GameInput.Pause => pause(state)
      case GameInput.Resume => resume(state)
      case GameInput.Restart => restart(state)
      case GameInput.Tick => tick(state)
      case GameInput.ChangeConfig(newConfig) => changeConfig(state, newConfig)
  
  private def move(state: GameState, dx: Int, dy: Int): (GameState, List[GameEvent]) =
    if !state.isPlaying then return (state, List.empty)
    
    state.currentPiece match
      case Some(piece) =>
        val moved = piece.move(dx, dy)
        if state.board.canPlace(moved) then
          val newState = state
            .withCurrentPiece(Some(moved))
            .withLastAction(GameInput.MoveDown)
          val event = GameEvent.PieceMoved(moved, piece.position, moved.position)
          (newState, List(event))
        else
          (state, List.empty)
      case None => (state, List.empty)
  
  private def rotate(state: GameState, clockwise: Boolean): (GameState, List[GameEvent]) =
    if !state.isPlaying then return (state, List.empty)
    
    state.currentPiece match
      case Some(piece) =>
        val rotated = if clockwise then piece.rotateCW else piece.rotateCCW
        if state.board.canPlace(rotated) then
          val newState = state
            .withCurrentPiece(Some(rotated))
            .withLastAction(if clockwise then GameInput.RotateCW else GameInput.RotateCCW)
          val event = GameEvent.PieceRotated(rotated, piece.rotation, rotated.rotation)
          (newState, List(event))
        else
          // Try wall kicks
          tryWallKicks(state, piece, rotated, clockwise)
      case None => (state, List.empty)
  
  private def tryWallKicks(state: GameState, original: Piece, rotated: Piece, clockwise: Boolean): (GameState, List[GameEvent]) =
    val kicks = wallKickOffsets(original, rotated)
    kicks.foldLeft((state, List.empty[GameEvent])) { (acc, offset) =>
      val (s, events) = acc
      if events.nonEmpty then acc
      else
        val kicked = rotated.move(offset._1, offset._2)
        if s.board.canPlace(kicked) then
          val newState = s
            .withCurrentPiece(Some(kicked))
            .withLastAction(if clockwise then GameInput.RotateCW else GameInput.RotateCCW)
          val event = GameEvent.PieceRotated(kicked, original.rotation, kicked.rotation)
          (newState, List(event))
        else
          acc
    }
  
  private def wallKickOffsets(from: Piece, to: Piece): List[(Int, Int)] =
    // Simplified wall kicks - in practice you'd use SRS or similar
    List((0, 0), (-1, 0), (1, 0), (-2, 0), (2, 0), (0, -1), (-1, -1), (1, -1))
  
  private def hardDrop(state: GameState): (GameState, List[GameEvent]) =
    if !state.isPlaying || !state.config.allowHardDrop then return (state, List.empty)
    
    state.currentPiece match
      case Some(piece) =>
        var current = piece
        var dropDistance = 0
        while state.board.canPlace(current.move(0, 1)) do
          current = current.move(0, 1)
          dropDistance += 1
        
        if dropDistance > 0 then
          val (lockedState, lockEvents) = lockPiece(state.withCurrentPiece(Some(current)))
          val dropEvent = GameEvent.PieceHardDropped(current, piece.position, current.position)
          val scoreEvent = GameEvent.ScoreChanged(state.score, lockedState.score)
          (lockedState, dropEvent :: scoreEvent :: lockEvents)
        else
          (state, List.empty)
      case None => (state, List.empty)
  
  private def hold(state: GameState): (GameState, List[GameEvent]) =
    if !state.isPlaying || !state.config.allowHold || !state.canHold then return (state, List.empty)
    
    state.currentPiece match
      case Some(current) =>
        state.heldPiece match
          case Some(heldDef) =>
            val newCurrent = Piece.spawn(heldDef, state.config.boardWidth)
            if state.board.canPlace(newCurrent) then
              val newState = state
                .withCurrentPiece(Some(newCurrent))
                .withHeldPiece(Some(current.definition))
                .withLastAction(GameInput.Hold)
              val event = GameEvent.HoldUsed(Some(current.definition), newCurrent)
              (newState, List(event))
            else
              (state, List.empty)
          case None =>
            val (nextPieceId, newRandomState) = spawnNextPiece(state.config, state.randomState)
            nextPieceId match
              case Some(pid) =>
                val registry = PieceRegistry.fromDefinitions(state.config.enabledPieces.map(pid => 
                  StandardPieces.all.getOrElse(pid, CustomPieces.all.getOrElse(pid, throw new IllegalArgumentException(s"Unknown piece: $pid")))
                ).toSeq)
                val newCurrent = Piece.spawn(registry.get(pid).get, state.config.boardWidth)
                val newState = state
                  .withCurrentPiece(Some(newCurrent))
                  .withHeldPiece(Some(current.definition))
                  .withRandomState(newRandomState)
                  .withLastAction(GameInput.Hold)
                val event = GameEvent.HoldUsed(None, newCurrent)
                (newState, List(event))
              case None => (state, List.empty)
      case None => (state, List.empty)
  
  private def pause(state: GameState): (GameState, List[GameEvent]) =
    if state.isPlaying then
      (state.withStatus(GameStatus.Paused), List(GameEvent.GamePaused))
    else (state, List.empty)
  
  private def resume(state: GameState): (GameState, List[GameEvent]) =
    if state.isPaused then
      (state.withStatus(GameStatus.Playing), List(GameEvent.GameResumed))
    else (state, List.empty)
  
  private def restart(state: GameState): (GameState, List[GameEvent]) =
    val newState = GameState.initial(state.config, state.randomState)
    (newState.withStatus(GameStatus.Playing), List(GameEvent.GameRestarted))
  
  private def tick(state: GameState): (GameState, List[GameEvent]) =
    if !state.isPlaying then return (state, List.empty)
    
    if !state.config.gravityEnabled then
      return (state, List.empty)
    
    // Move piece down by one cell
    val (newState, events) = move(state, 0, 1)
    if newState.currentPiece.isEmpty || newState.currentPiece.get.position.y == state.currentPiece.get.position.y then
      // Piece couldn't move down, lock it
      val (lockedState, lockEvents) = lockPiece(newState)
      (lockedState, events ++ lockEvents)
    else
      (newState, events)
  
  private def lockPiece(state: GameState): (GameState, List[GameEvent]) =
    state.currentPiece match
      case Some(piece) =>
        val boardWithPiece = state.board.place(piece)
        val (clearedBoard, linesCleared) = boardWithPiece.clearLines
        
        var newState = state
          .withBoard(clearedBoard)
          .withCurrentPiece(None)
          .resetDropCounter
          .resetHold
        
        var events = List(GameEvent.PieceLocked(piece))
        
        if linesCleared > 0 then
          val scoreGain = state.config.scoring.scoreForLines(linesCleared, state.level)
          newState = newState
            .addScore(scoreGain)
            .addLinesCleared(linesCleared)
          events = events :+ GameEvent.LinesCleared(linesCleared, clearedBoard)
          events = events :+ GameEvent.ScoreChanged(state.score, newState.score)
          
          // Check level up
          val newLevel = state.config.levelConfig.levelForLines(newState.linesCleared)
          if newLevel > state.level then
            newState = newState.setLevel(newLevel).setDropInterval(state.config.levelConfig.dropIntervalForLevel(newLevel))
            events = events :+ GameEvent.LevelChanged(state.level, newLevel)
        
        // Spawn next piece (use the queued next piece if available, otherwise generate new)
        val nextPiece = state.nextPiece match
          case Some(queuedPiece) =>
            // Generate another piece for the preview queue
            val (nextPieceId, newRandomState) = spawnNextPiece(state.config, state.randomState)
            nextPieceId match
              case Some(pid) =>
                val registry = PieceRegistry.fromDefinitions(state.config.enabledPieces.map(pid => 
                  StandardPieces.all.getOrElse(pid, CustomPieces.all.getOrElse(pid, throw new IllegalArgumentException(s"Unknown piece: $pid")))
                ).toSeq)
                val previewPiece = Piece.spawn(registry.get(pid).get, state.config.boardWidth)
                (queuedPiece, previewPiece, newRandomState)
              case None =>
                (queuedPiece, null, state.randomState)
          case None =>
            // First piece after lock - generate two pieces
            val (pieceId1, rs1) = spawnNextPiece(state.config, state.randomState)
            val (pieceId2, rs2) = spawnNextPiece(state.config, rs1)
            (pieceId1, pieceId2, rs2) match
              case (Some(pid1), Some(pid2), newRs) =>
                val registry = PieceRegistry.fromDefinitions(state.config.enabledPieces.map(pid => 
                  StandardPieces.all.getOrElse(pid, CustomPieces.all.getOrElse(pid, throw new IllegalArgumentException(s"Unknown piece: $pid")))
                ).toSeq)
                val currentPiece = Piece.spawn(registry.get(pid1).get, state.config.boardWidth)
                val previewPiece = Piece.spawn(registry.get(pid2).get, state.config.boardWidth)
                (currentPiece, previewPiece, newRs)
              case _ =>
                (null, null, state.randomState)
        
        nextPiece match
          case (currentPiece: Piece, previewPiece: Piece, newRandomState: RandomState) =>
            if clearedBoard.canPlace(currentPiece) then
              newState = newState
                .withCurrentPiece(Some(currentPiece))
                .withNextPiece(Some(previewPiece))
                .withRandomState(newRandomState)
              events = events :+ GameEvent.PieceSpawned(currentPiece)
            else
              // Game over
              newState = newState
                .withStatus(GameStatus.GameOver)
                .withRandomState(newRandomState)
              events = events :+ GameEvent.GameOver(newState.score, newState.level, newState.linesCleared)
          case (null, _, _) | (_, null, _) | (_, _, null) =>
            newState = newState.withStatus(GameStatus.GameOver)
            events = events :+ GameEvent.GameOver(newState.score, newState.level, newState.linesCleared)
        
        (newState, events)
      case None => (state, List.empty)
  
  private def spawnNextPiece(config: VariantConfig, randomState: RandomState): (Option[PieceId], RandomState) =
    val registry = PieceRegistry.fromDefinitions(config.enabledPieces.map(pid => 
      StandardPieces.all.getOrElse(pid, CustomPieces.all.getOrElse(pid, throw new IllegalArgumentException(s"Unknown piece: $pid")))
    ).toSeq)
    val generator = PieceGenerator.forStrategy(config.pieceGeneration.strategy)
    generator.nextPiece(registry, config.pieceGeneration, randomState)
  
  private def changeConfig(state: GameState, newConfig: VariantConfig): (GameState, List[GameEvent]) =
    newConfig.validate match
      case Left(error) => (state, List.empty)
      case Right(validConfig) =>
        val dropInterval = validConfig.levelConfig.dropIntervalForLevel(state.level)
        val newState = state
          .withConfig(validConfig)
          .setDropInterval(dropInterval)
        (newState, List(GameEvent.ConfigChanged(validConfig)))