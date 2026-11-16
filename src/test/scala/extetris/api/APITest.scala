package extetris.api

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.model.{GameState, GameInput, GameStatus}
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig}
import extetris.pieces.{PieceId, PieceDefinition, PieceColor, PieceRegistry, RotationBehavior}
import extetris.model.Position
import extetris.engine.GameEngine

class APITest extends AnyFunSuite with Matchers:
  
  test("Create variant from config"):
    val config = VariantConfig.default
    val result = ExTetrisAPI.createVariant(config)
    result.isRight shouldBe true
  
  test("Default variants available"):
    ExTetrisAPI.defaultVariant.name shouldBe "Default"
    ExTetrisAPI.classicVariant.name shouldBe "Classic ExTetris"
    ExTetrisAPI.extendedVariant.name shouldBe "Extended ExTetris"
  
  test("Define standard piece"):
    val piece = ExTetrisAPI.defineStandardPiece(
      PieceId("TestI"), "Test I", PieceColor(0, 1, 1),
      Set(Position(0, 0), Position(1, 0), Position(2, 0), Position(3, 0)),
      RotationBehavior.Limited(2)
    )
    piece.id shouldBe PieceId("TestI")
    piece.name shouldBe "Test I"
    piece.behavior shouldBe RotationBehavior.Limited(2)
  
  test("Define custom piece"):
    val piece = ExTetrisAPI.defineCustomPiece(
      PieceId("MyPiece"), "My Piece", PieceColor(1, 0, 1),
      Set(Position(0, 0), Position(1, 0), Position(0, 1)),
      RotationBehavior.Full
    )
    piece.id shouldBe PieceId("MyPiece")
    piece.rotationCount shouldBe 4
  
  test("New game creation"):
    val state = ExTetrisAPI.newGame(VariantConfig.default, Some(12345L))
    state.status shouldBe GameStatus.Playing
    state.board.isEmpty shouldBe true
    state.currentPiece.isDefined shouldBe true
    state.config.name shouldBe "Default"
  
  test("Apply input"):
    val state = ExTetrisAPI.newGame(VariantConfig.default, Some(12345L))
    val (newState, events) = ExTetrisAPI.applyInput(state, GameInput.MoveRight)
    newState.currentPiece.get.position.x should be > state.currentPiece.get.position.x
  
  test("Tick"):
    val state = ExTetrisAPI.newGame(VariantConfig.default, Some(12345L))
    val (newState, _) = ExTetrisAPI.tick(state)
    newState.dropCounter shouldBe state.dropCounter + 1
  
  test("State inspection"):
    val state = ExTetrisAPI.newGame(VariantConfig.default, Some(12345L))
    ExTetrisAPI.getBoard(state).isEmpty shouldBe true
    ExTetrisAPI.getCurrentPiece(state).isDefined shouldBe true
    ExTetrisAPI.getScore(state) shouldBe 0
    ExTetrisAPI.getLevel(state) shouldBe 1
    ExTetrisAPI.getLinesCleared(state) shouldBe 0
    ExTetrisAPI.getStatus(state) shouldBe GameStatus.Playing
    ExTetrisAPI.isGameOver(state) shouldBe false
    ExTetrisAPI.isPlaying(state) shouldBe true
  
  test("Serialization roundtrip"):
    val state = ExTetrisAPI.newGame(VariantConfig.default, Some(12345L))
    val json = ExTetrisAPI.serializeGame(state)
    val restored = ExTetrisAPI.deserializeGame(json)
    restored.isRight shouldBe true
    restored.getOrElse(fail("Deserialization failed")).board.grid shouldBe state.board.grid
  
  test("Replay recording and playback"):
    val state = ExTetrisAPI.newGame(VariantConfig.default, Some(12345L))
    val inputs = List(GameInput.MoveRight, GameInput.RotateCW, GameInput.HardDrop)
    val replay = ExTetrisAPI.recordReplay(state, inputs)
    replay.inputs shouldBe inputs
    
    val results = ExTetrisAPI.playReplay(replay)
    results.size shouldBe inputs.size
  
  test("Config builders"):
    val scoring = ExTetrisAPI.scoringConfig(single = 200, double = 400)
    scoring.singleLine shouldBe 200
    scoring.doubleLine shouldBe 400
    
    val levels = ExTetrisAPI.levelConfig(linesPerLevel = 5)
    levels.linesPerLevel shouldBe 5
    
    val gen = ExTetrisAPI.generationConfig(
      strategy = extetris.config.GenerationStrategy.Uniform,
      seed = Some(12345L)
    )
    gen.strategy shouldBe extetris.config.GenerationStrategy.Uniform
    gen.seed shouldBe Some(12345L)
  
  test("Piece registry access"):
    val defaultReg = ExTetrisAPI.defaultRegistry
    defaultReg.enabledIds should contain allElementsOf PieceId.standardIds
    
    val emptyReg = ExTetrisAPI.emptyRegistry
    emptyReg.enabledIds shouldBe empty
    
    val customReg = ExTetrisAPI.registryFrom(Seq(
      ExTetrisAPI.defineCustomPiece(PieceId("Test"), "Test", PieceColor(1,0,0), Set(Position(0,0)))
    ))
    customReg.enabledIds should contain(PieceId("Test"))
  
  test("Custom variant creation without engine modification"):
    // This test demonstrates that a programmer can create a completely new variant
    // without modifying any engine internals
    
    // 1. Define a custom piece
    val pentomino = ExTetrisAPI.defineCustomPiece(
      PieceId("Pentomino"), "Pentomino", PieceColor(0.5, 0.5, 1.0),
      Set(Position(0, 0), Position(1, 0), Position(2, 0), Position(1, 1), Position(1, 2)),
      RotationBehavior.Full
    )
    
    // 2. Create a custom registry with the custom piece
    val customRegistry = ExTetrisAPI.registryFrom(
      extetris.pieces.StandardPieces.all.values.toSeq ++ 
      extetris.pieces.CustomPieces.all.values.toSeq :+ pentomino
    )
    
    // 3. Create a variant using the custom piece
    val customVariant = VariantConfig.builder("Pentomino Tetris")
      .boardSize(12, 24)
      .enabledPieces(PieceId.standardIds + PieceId("Pentomino"))
      .pieceGeneration(ExTetrisAPI.generationConfig(
        strategy = extetris.config.GenerationStrategy.Weighted,
        weights = Map(PieceId("Pentomino") -> 5) ++ PieceId.standardIds.map(_ -> 1).toMap
      ))
      .scoring(ExTetrisAPI.scoringConfig(tetris = 2000))
      .levelConfig(ExTetrisAPI.levelConfig(linesPerLevel = 15))
      .allowHold(true)
      .allowHardDrop(true)
      .previewCount(3)
      .build()
    
    // 4. Validate and run the variant with custom registry
    customVariant.validate.isRight shouldBe true
    val state = ExTetrisAPI.newGame(customVariant, Some(99999L), Some(customRegistry))
    state.config.enabledPieces should contain(PieceId("Pentomino"))
    state.config.boardWidth shouldBe 12
    state.config.boardHeight shouldBe 24
    
    // 5. Play a few moves
    var currentState = state
    for _ <- 1 to 10 do
      val (next, _) = ExTetrisAPI.applyInput(currentState, GameInput.Tick)
      currentState = next
    
    currentState.status shouldBe GameStatus.Playing