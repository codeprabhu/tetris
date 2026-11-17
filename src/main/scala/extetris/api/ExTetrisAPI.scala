package extetris.api

import extetris.model.{GameState, GameInput, GameEvent}
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig, ValidationError}
import extetris.pieces.{PieceId, PieceDefinition, PieceColor, PieceRegistry, RotationBehavior}
import extetris.generation.RandomState
import extetris.persistence.{GameSerializer, GamePersistence}
import extetris.replay.{Replay, ReplayPlayer}
import extetris.engine.GameEngine

object ExTetrisAPI:
  // Configuration
  def createVariant(config: VariantConfig): Either[ValidationError, VariantConfig] = config.validate
  
  def defaultVariant: VariantConfig = VariantConfig.default
  def classicVariant: VariantConfig = VariantConfig.classic
  def extendedVariant: VariantConfig = VariantConfig.extended
  
  // Piece Definition
  def defineStandardPiece(
    id: PieceId,
    name: String,
    color: PieceColor,
    cells: Set[extetris.model.Position],
    behavior: RotationBehavior = RotationBehavior.Full
  ): PieceDefinition =
    PieceDefinition.standard(id, name, color, cells, behavior)
  
  def defineCustomPiece(
    id: PieceId,
    name: String,
    color: PieceColor,
    cells: Set[extetris.model.Position],
    behavior: RotationBehavior = RotationBehavior.Full
  ): PieceDefinition =
    PieceDefinition.custom(id, name, color, cells, behavior)
  
  // Game Control
  def newGame(config: VariantConfig, seed: Option[Long] = None): GameState =
    newGame(config, seed, None)
  
  def newGame(config: VariantConfig, seed: Option[Long], customRegistry: Option[extetris.pieces.PieceRegistry]): GameState =
    val randomState = seed.map(RandomState.fromSeed).getOrElse(RandomState.random())
    var state = GameState.initial(config, randomState).withStatus(extetris.model.GameStatus.Playing)
    // Spawn first piece and next piece preview
    val (firstPiece, nextPiece, finalRandomState) = spawnFirstAndNextPiece(config, state.randomState, customRegistry)
    firstPiece.foreach { piece =>
      if state.board.canPlace(piece) then
        state = state
          .withCurrentPiece(Some(piece))
          .withNextPiece(nextPiece)
          .withRandomState(finalRandomState)
    }
    state
  
  private def spawnFirstAndNextPiece(config: VariantConfig, randomState: extetris.generation.RandomState, customRegistry: Option[extetris.pieces.PieceRegistry] = None): (Option[extetris.pieces.Piece], Option[extetris.pieces.Piece], extetris.generation.RandomState) =
    val registry = customRegistry.getOrElse(
      PieceRegistry.fromDefinitions(config.enabledPieces.map(pid =>
        extetris.pieces.StandardPieces.all.getOrElse(pid, extetris.pieces.CustomPieces.all.getOrElse(pid, throw new IllegalArgumentException(s"Unknown piece: $pid")))
      ).toSeq)
    )
    val generator = extetris.generation.PieceGenerator.forStrategy(config.pieceGeneration.strategy)
    
    val (firstPieceId, rs1) = generator.nextPiece(registry, config.pieceGeneration, randomState)
    val (nextPieceId, rs2) = generator.nextPiece(registry, config.pieceGeneration, rs1)
    
    (firstPieceId, nextPieceId, rs2) match
      case (Some(id1), Some(id2), finalRs) =>
        val firstPiece = extetris.pieces.Piece.spawn(registry.get(id1).get, config.boardWidth)
        val nextPiece = extetris.pieces.Piece.spawn(registry.get(id2).get, config.boardWidth)
        (Some(firstPiece), Some(nextPiece), finalRs)
      case (Some(id1), None, finalRs) =>
        val firstPiece = extetris.pieces.Piece.spawn(registry.get(id1).get, config.boardWidth)
        (Some(firstPiece), None, finalRs)
      case _ =>
        (None, None, randomState)
  
  private def spawnNextPiece(config: VariantConfig, randomState: extetris.generation.RandomState, customRegistry: Option[extetris.pieces.PieceRegistry] = None): (Option[extetris.pieces.PieceId], extetris.generation.RandomState) =
    val registry = customRegistry.getOrElse(
      PieceRegistry.fromDefinitions(config.enabledPieces.map(pid =>
        extetris.pieces.StandardPieces.all.getOrElse(pid, extetris.pieces.CustomPieces.all.getOrElse(pid, throw new IllegalArgumentException(s"Unknown piece: $pid")))
      ).toSeq)
    )
    val generator = extetris.generation.PieceGenerator.forStrategy(config.pieceGeneration.strategy)
    generator.nextPiece(registry, config.pieceGeneration, randomState)
  
  def applyInput(state: GameState, input: GameInput): (GameState, List[GameEvent]) =
    GameEngine.step(state, input)
  
  def tick(state: GameState): (GameState, List[GameEvent]) =
    GameEngine.step(state, GameInput.Tick)
  
  // State Inspection
  def getBoard(state: GameState): extetris.board.Board = state.board
  def getCurrentPiece(state: GameState): Option[extetris.pieces.Piece] = state.currentPiece
  def getNextPiece(state: GameState): Option[extetris.pieces.Piece] = state.nextPiece
  def getHeldPiece(state: GameState): Option[PieceDefinition] = state.heldPiece
  def getScore(state: GameState): Long = state.score
  def getLevel(state: GameState): Int = state.level
  def getLinesCleared(state: GameState): Int = state.linesCleared
  def getStatus(state: GameState): extetris.model.GameStatus = state.status
  def getConfig(state: GameState): VariantConfig = state.config
  def isGameOver(state: GameState): Boolean = state.isGameOver
  def isPlaying(state: GameState): Boolean = state.isPlaying
  
  // Persistence
  def saveGame(state: GameState, path: String): Either[Exception, Unit] =
    GamePersistence.save(state, path)
  
  def loadGame(path: String): Either[Exception, GameState] =
    GamePersistence.load(path)
  
  def serializeGame(state: GameState): String =
    GameSerializer.serialize(state)
  
  def deserializeGame(json: String): Either[Exception, GameState] =
    GameSerializer.deserialize(json)
  
  // Replay
  def recordReplay(initialState: GameState, inputs: List[GameInput]): Replay =
    Replay.record(initialState, inputs)
  
  def playReplay(replay: Replay): List[(GameState, GameInput, List[GameEvent])] =
    ReplayPlayer.play(replay)
  
  // Configuration Builders
  def scoringConfig(
    single: Int = 100,
    double: Int = 300,
    triple: Int = 500,
    tetris: Int = 800,
    softDrop: Int = 1,
    hardDrop: Int = 2,
    levelMultiplier: Boolean = true
  ): ScoringConfig = ScoringConfig(single, double, triple, tetris, softDrop, hardDrop, levelMultiplier)
  
  def levelConfig(
    linesPerLevel: Int = 10,
    initialDropInterval: Int = 1000,
    minDropInterval: Int = 50,
    dropIntervalDecrease: Int = 50,
    maxLevel: Int = 20
  ): LevelConfig = LevelConfig(linesPerLevel, initialDropInterval, minDropInterval, dropIntervalDecrease, maxLevel)
  
  def generationConfig(
    strategy: extetris.config.GenerationStrategy = extetris.config.GenerationStrategy.Weighted,
    weights: Map[PieceId, Int] = Map.empty,
    seed: Option[Long] = None
  ): PieceGenerationConfig = PieceGenerationConfig(strategy, weights, seed)
  
  // Piece Registry
  def defaultRegistry: PieceRegistry = PieceRegistry.default
  def emptyRegistry: PieceRegistry = PieceRegistry.empty
  def registryFrom(definitions: Seq[PieceDefinition]): PieceRegistry = PieceRegistry.fromDefinitions(definitions)
  
  // Internal helper for spawning next piece
  private def spawnNextPiece(config: VariantConfig, randomState: extetris.generation.RandomState): (Option[PieceId], extetris.generation.RandomState) =
    val registry = PieceRegistry.fromDefinitions(config.enabledPieces.map(pid =>
      extetris.pieces.StandardPieces.all.getOrElse(pid, extetris.pieces.CustomPieces.all.getOrElse(pid, throw new IllegalArgumentException(s"Unknown piece: $pid")))
    ).toSeq)
    val generator = extetris.generation.PieceGenerator.forStrategy(config.pieceGeneration.strategy)
    generator.nextPiece(registry, config.pieceGeneration, randomState)