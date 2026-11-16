package extetris.replay

import extetris.model.{GameState, GameInput, GameEvent}
import extetris.config.VariantConfig
import extetris.generation.RandomState
import extetris.engine.GameEngine
import extetris.api.ExTetrisAPI

object ReplayPlayer:
  def play(replay: Replay): List[(GameState, GameInput, List[GameEvent])] =
    var state = ExTetrisAPI.newGame(replay.config, Some(replay.initialRandomState.currentSeed))
    val results = scala.collection.mutable.ListBuffer.empty[(GameState, GameInput, List[GameEvent])]
    
    for input <- replay.inputs do
      val (newState, events) = GameEngine.step(state, input)
      results += ((state, input, events))
      state = newState
    
    results.toList
  
  def playStepByStep(replay: Replay): Iterator[(GameState, GameInput, List[GameEvent], GameState)] =
    new Iterator[(GameState, GameInput, List[GameEvent], GameState)]:
      var state = ExTetrisAPI.newGame(replay.config, Some(replay.initialRandomState.currentSeed))
      var index = 0
      
      def hasNext: Boolean = index < replay.inputs.length
      
      def next(): (GameState, GameInput, List[GameEvent], GameState) =
        if !hasNext then throw new NoSuchElementException("No more inputs in replay")
        val input = replay.inputs(index)
        val (newState, events) = GameEngine.step(state, input)
        val result = (state, input, events, newState)
        state = newState
        index += 1
        result
  
  def verify(replay: Replay): Boolean =
    val results = playStepByStep(replay).toList
    // Compare replayed events with recorded events
    val replayedEvents = results.flatMap(_._3)
    val recordedEvents = replay.events
    
    if replayedEvents.length != recordedEvents.length then return false
    
    replayedEvents.zip(recordedEvents).forall { (replayed, recorded) =>
      // Compare event types and key data
      (replayed, recorded) match
        case (GameEvent.PieceSpawned(p1), GameEvent.PieceSpawned(p2)) => p1.definition.id == p2.definition.id
        case (GameEvent.PieceMoved(p1, f1, t1), GameEvent.PieceMoved(p2, f2, t2)) => p1.definition.id == p2.definition.id && f1 == f2 && t1 == t2
        case (GameEvent.PieceRotated(p1, f1, t1), GameEvent.PieceRotated(p2, f2, t2)) => p1.definition.id == p2.definition.id && f1 == f2 && t1 == t2
        case (GameEvent.PieceHardDropped(p1, f1, t1), GameEvent.PieceHardDropped(p2, f2, t2)) => p1.definition.id == p2.definition.id && f1 == f2 && t1 == t2
        case (GameEvent.PieceLocked(p1), GameEvent.PieceLocked(p2)) => p1.definition.id == p2.definition.id
        case (GameEvent.LinesCleared(c1, _), GameEvent.LinesCleared(c2, _)) => c1 == c2
        case (GameEvent.ScoreChanged(o1, n1), GameEvent.ScoreChanged(o2, n2)) => o1 == o2 && n1 == n2
        case (GameEvent.LevelChanged(o1, n1), GameEvent.LevelChanged(o2, n2)) => o1 == o2 && n1 == n2
        case (GameEvent.GameOver(s1, l1, li1), GameEvent.GameOver(s2, l2, li2)) => s1 == s2 && l1 == l2 && li1 == li2
        case (GameEvent.PieceSpawned(_), _) => false
        case (GameEvent.PieceMoved(_, _, _), _) => false
        case (GameEvent.PieceRotated(_, _, _), _) => false
        case (GameEvent.PieceHardDropped(_, _, _), _) => false
        case (GameEvent.PieceLocked(_), _) => false
        case (GameEvent.LinesCleared(_, _), _) => false
        case (GameEvent.ScoreChanged(_, _), _) => false
        case (GameEvent.LevelChanged(_, _), _) => false
        case (GameEvent.GameOver(_, _, _), _) => false
        case (GameEvent.GamePaused, GameEvent.GamePaused) => true
        case (GameEvent.GameResumed, GameEvent.GameResumed) => true
        case (GameEvent.GameRestarted, GameEvent.GameRestarted) => true
        case (GameEvent.ConfigChanged(c1), GameEvent.ConfigChanged(c2)) => c1 == c2
        case (GameEvent.HoldUsed(h1, c1), GameEvent.HoldUsed(h2, c2)) => h1 == h2 && c1.definition.id == c2.definition.id
        case _ => false
    }