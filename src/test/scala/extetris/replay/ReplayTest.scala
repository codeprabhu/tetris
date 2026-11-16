package extetris.replay

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.model.{GameState, GameInput, GameEvent}
import extetris.config.VariantConfig
import extetris.api.ExTetrisAPI
import extetris.engine.GameEngine

class ReplayTest extends AnyFunSuite with Matchers:
  
  def createTestState: GameState =
    ExTetrisAPI.newGame(VariantConfig.default, Some(12345L))
  
  test("Replay records initial state"):
    val state = createTestState
    val replay = Replay.record(state, List.empty)
    
    replay.config.name shouldBe state.config.name
    replay.initialRandomState.currentSeed shouldBe state.randomState.currentSeed
    replay.inputs shouldBe empty
    replay.events shouldBe empty
  
  test("Replay records inputs"):
    val state = createTestState
    val inputs = List(GameInput.MoveLeft, GameInput.RotateCW, GameInput.HardDrop)
    val replay = Replay.record(state, inputs)
    
    replay.inputs shouldBe inputs
  
  test("Replay playback reproduces game"):
    val config = VariantConfig.default.withEnabledPieces(Set(extetris.pieces.PieceId.O))
    val state = ExTetrisAPI.newGame(config, Some(12345L))
    val inputs = List(
      GameInput.MoveRight, GameInput.MoveRight,
      GameInput.RotateCW, GameInput.HardDrop,
      GameInput.MoveLeft, GameInput.HardDrop
    )
    
    val replay = Replay.record(state, inputs)
    val results = ReplayPlayer.playStepByStep(replay).toList
    
    results.size shouldBe inputs.size
    
    // Verify final state matches
    val finalState = results.last._4
    val expectedState = inputs.foldLeft(state) { (s, input) =>
      GameEngine.step(s, input)._1
    }
    
    finalState.board.grid shouldBe expectedState.board.grid
    finalState.score shouldBe expectedState.score
    finalState.level shouldBe expectedState.level
  
  test("Step-by-step replay iterator"):
    val config = VariantConfig.default.withEnabledPieces(Set(extetris.pieces.PieceId.O))
    val state = ExTetrisAPI.newGame(config, Some(12345L))
    val inputs = List(GameInput.MoveRight, GameInput.HardDrop, GameInput.MoveLeft)
    
    val replay = Replay.record(state, inputs)
    val iterator = ReplayPlayer.playStepByStep(replay)
    
    val steps = iterator.toList
    steps.size shouldBe inputs.size
    
    steps.foreach { case (before, input, events, after) =>
      val (expectedAfter, expectedEvents) = GameEngine.step(before, input)
      after shouldBe expectedAfter
      events shouldBe expectedEvents
    }
  
  test("Replay verification"):
    val config = VariantConfig.default.withEnabledPieces(Set(extetris.pieces.PieceId.O))
    val state = ExTetrisAPI.newGame(config, Some(12345L))
    val inputs = List(GameInput.HardDrop, GameInput.HardDrop)
    
    // Play once to get events
    var currentState = state
    val allEvents = scala.collection.mutable.ListBuffer.empty[GameEvent]
    for input <- inputs do
      val (newState, events) = GameEngine.step(currentState, input)
      allEvents ++= events
      currentState = newState
    
    val replay = Replay.record(state, inputs).withEvents(allEvents.toList)
    ReplayPlayer.verify(replay) shouldBe true
  
  test("Replay with different seed fails verification"):
    val config = VariantConfig.default.withEnabledPieces(extetris.pieces.PieceId.standardIds).withBoardSize(10, 10)
    val state1 = ExTetrisAPI.newGame(config, Some(11111L))
    val state2 = ExTetrisAPI.newGame(config, Some(22222L))
    val inputs = List.fill(50)(GameInput.HardDrop)
    
    var currentState = state1
    val events = scala.collection.mutable.ListBuffer.empty[GameEvent]
    for input <- inputs do
      val (newState, evts) = GameEngine.step(currentState, input)
      events ++= evts
      currentState = newState
      if currentState.isGameOver then ()
    
    val replay = Replay.record(state2, inputs).withEvents(events.toList)
    ReplayPlayer.verify(replay) shouldBe false
  
  test("Deterministic replay from same seed"):
    val config = VariantConfig.default
    val seed = 12345L
    val state1 = ExTetrisAPI.newGame(config, Some(seed))
    val state2 = ExTetrisAPI.newGame(config, Some(seed))
    
    val inputs = List(
      GameInput.MoveRight, GameInput.RotateCW, GameInput.MoveLeft,
      GameInput.HardDrop, GameInput.MoveRight, GameInput.HardDrop
    )
    
    val replay1 = Replay.record(state1, inputs)
    val replay2 = Replay.record(state2, inputs)
    
    val results1 = ReplayPlayer.playStepByStep(replay1).toList
    val results2 = ReplayPlayer.playStepByStep(replay2).toList
    
    results1.map(_._4.board.grid) shouldBe results2.map(_._4.board.grid)
    results1.map(_._4.score) shouldBe results2.map(_._4.score)