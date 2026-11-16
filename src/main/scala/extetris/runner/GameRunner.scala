package extetris.runner

import extetris.model.{GameState, GameInput, GameEvent, GameStatus}
import extetris.config.VariantConfig
import extetris.generation.RandomState
import extetris.engine.GameEngine
import extetris.api.ExTetrisAPI
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.*
import scala.util.{Try, Success, Failure}

class GameRunner(initialState: GameState)(using ExecutionContext):
  private var currentState: GameState = initialState
  private var listeners: List[GameState => Unit] = List.empty
  private var running = false
  private var tickTask: Option[Future[Unit]] = None
  
  def state: GameState = currentState
  def isRunning: Boolean = running
  
  def subscribe(listener: GameState => Unit): () => Unit =
    listeners = listener :: listeners
    () => listeners = listeners.filterNot(_ == listener)
  
  def start(): Unit =
    if !running then
      running = true
      scheduleTick()
  
  def stop(): Unit =
    running = false
    tickTask = None
  
  def pause(): Unit =
    if currentState.isPlaying then
      val (newState, events) = GameEngine.step(currentState, GameInput.Pause)
      updateState(newState, events)
  
  def resume(): Unit =
    if currentState.isPaused then
      val (newState, events) = GameEngine.step(currentState, GameInput.Resume)
      updateState(newState, events)
  
  def restart(): Unit =
    val (newState, events) = GameEngine.step(currentState, GameInput.Restart)
    updateState(newState, events)
    if running then scheduleTick()
  
  def handleInput(input: GameInput): Unit =
    if currentState.isPlaying || input == GameInput.Restart then
      val (newState, events) = GameEngine.step(currentState, input)
      updateState(newState, events)
  
  def changeConfig(config: VariantConfig): Unit =
    val (newState, events) = GameEngine.step(currentState, GameInput.ChangeConfig(config))
    updateState(newState, events)
  
  private def updateState(newState: GameState, events: List[GameEvent]): Unit =
    currentState = newState
    notifyListeners()
    if newState.isPlaying && running then scheduleTick()
  
  private def notifyListeners(): Unit =
    listeners.foreach(_(currentState))
  
  private def scheduleTick(): Unit =
    if running && currentState.isPlaying then
      val delay = currentState.dropInterval.millis
      tickTask = Some(
        Future {
          Thread.sleep(delay.toMillis)
          if running && currentState.isPlaying then
            handleInput(GameInput.Tick)
        }
      )
  
  def save(path: String): Either[Exception, Unit] =
    extetris.persistence.GamePersistence.save(currentState, path)
  
  def load(path: String): Either[Exception, Unit] =
    extetris.persistence.GamePersistence.load(path).map { loadedState =>
      currentState = loadedState
      notifyListeners()
      if running && currentState.isPlaying then scheduleTick()
    }

object GameRunner:
  def apply(config: VariantConfig, seed: Option[Long] = None)(using ExecutionContext): GameRunner =
    val initialState = ExTetrisAPI.newGame(config, seed)
    new GameRunner(initialState)
  
  def fromState(state: GameState)(using ExecutionContext): GameRunner =
    new GameRunner(state)