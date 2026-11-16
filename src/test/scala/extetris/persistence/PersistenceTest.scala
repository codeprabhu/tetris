package extetris.persistence

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.model.{GameState, GameInput, GameEvent}
import extetris.config.VariantConfig
import extetris.api.ExTetrisAPI
import java.nio.file.{Files, Path}
import scala.util.Using

class PersistenceTest extends AnyFunSuite with Matchers:
  
  def createTestState: GameState =
    ExTetrisAPI.newGame(VariantConfig.default, Some(12345L))
  
  test("Serialize and deserialize game state"):
    val state = createTestState
    val json = GameSerializer.serialize(state)
    json.nonEmpty shouldBe true
    json should include("board")
    json should include("score")
    json should include("config")
    
    val decoded = GameSerializer.deserialize(json)
    decoded.isRight shouldBe true
    val restored = decoded.getOrElse(fail("Deserialization failed"))
    
    restored.board.grid shouldBe state.board.grid
    restored.score shouldBe state.score
    restored.level shouldBe state.level
    restored.linesCleared shouldBe state.linesCleared
    restored.config.name shouldBe state.config.name
    restored.randomState.currentSeed shouldBe state.randomState.currentSeed
  
  test("Save and load to file"):
    val state = createTestState
    val tempFile = Files.createTempFile("extetris-test-", ".json")
    
    try
      GamePersistence.save(state, tempFile.toString).isRight shouldBe true
      
      val loaded = GamePersistence.load(tempFile.toString)
      loaded.isRight shouldBe true
      val restored = loaded.getOrElse(fail("Load failed"))
      
      restored.board.grid shouldBe state.board.grid
      restored.score shouldBe state.score
      restored.level shouldBe state.level
    finally
      Files.deleteIfExists(tempFile)
  
  test("Load non-existent file fails"):
    val result = GamePersistence.load("/non/existent/path.json")
    result.isLeft shouldBe true
    result.swap.foreach(_ shouldBe a[java.io.FileNotFoundException])
  
  test("Save with invalid path fails"):
    val state = createTestState
    val result = GamePersistence.save(state, "/invalid/path/that/does/not/exist/save.json")
    result.isLeft shouldBe true
  
  test("Replay serialization"):
    val replay = extetris.replay.Replay.record(createTestState, List(GameInput.MoveLeft, GameInput.RotateCW))
    val json = extetris.replay.Replay.serialize(replay)
    json.nonEmpty shouldBe true
    
    val decoded = extetris.replay.Replay.deserialize(json)
    decoded.isRight shouldBe true
    val restored = decoded.getOrElse(fail("Replay deserialization failed"))
    
    restored.config.name shouldBe replay.config.name
    restored.inputs shouldBe replay.inputs
    restored.initialRandomState.currentSeed shouldBe replay.initialRandomState.currentSeed
  
  test("Save and load replay"):
    val replay = extetris.replay.Replay.record(createTestState, List(GameInput.MoveLeft, GameInput.RotateCW))
    val tempFile = Files.createTempFile("extetris-replay-", ".json")
    
    try
      extetris.replay.Replay.save(replay, tempFile.toString).isRight shouldBe true
      
      val loaded = extetris.replay.Replay.load(tempFile.toString)
      loaded.isRight shouldBe true
      val restored = loaded.getOrElse(fail("Replay load failed"))
      
      restored.inputs shouldBe replay.inputs
    finally
      Files.deleteIfExists(tempFile)
  
  test("Game state with piece on board serializes correctly"):
    val config = VariantConfig.default.withEnabledPieces(Set(extetris.pieces.PieceId.O))
    var state = ExTetrisAPI.newGame(config, Some(12345L))
    
    // Make a move to ensure piece is on board
    val (stateAfterMove, _) = extetris.engine.GameEngine.step(state, GameInput.MoveRight)
    state = stateAfterMove
    
    val json = GameSerializer.serialize(state)
    val decoded = GameSerializer.deserialize(json)
    decoded.isRight shouldBe true
    
    val restored = decoded.getOrElse(fail("Deserialization failed"))
    restored.currentPiece.isDefined shouldBe true
    restored.currentPiece.get.position shouldBe state.currentPiece.get.position
  
  test("Game state with held piece serializes correctly"):
    val config = VariantConfig.default.withAllowHold(true)
    var state = ExTetrisAPI.newGame(config, Some(12345L))
    
    val (stateAfterHold, _) = extetris.engine.GameEngine.step(state, GameInput.Hold)
    state = stateAfterHold
    
    val json = GameSerializer.serialize(state)
    val decoded = GameSerializer.deserialize(json)
    decoded.isRight shouldBe true
    
    val restored = decoded.getOrElse(fail("Deserialization failed"))
    restored.heldPiece.isDefined shouldBe true
    restored.heldPiece.get.id shouldBe state.heldPiece.get.id