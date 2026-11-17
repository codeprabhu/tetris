package extetris.runner

import extetris.model.{GameState, GameInput, GameEvent, GameStatus}
import extetris.config.VariantConfig
import extetris.generation.RandomState
import extetris.engine.GameEngine
import extetris.api.ExTetrisAPI
import scala.concurrent.ExecutionContext
import scalafx.animation.AnimationTimer
import scalafx.application.Platform

class GameRunner(initialState: GameState)(using ExecutionContext):
  private var currentState: GameState = initialState
  private var listeners: List[GameState => Unit] = List.empty
  private var running = false
  private var animationTimer: Option[AnimationTimer] = None
  private var lastTickTime: Long = 0
  
  def state: GameState = currentState
  def isRunning: Boolean = running
  
  def subscribe(listener: GameState => Unit): () => Unit =
    listeners = listener :: listeners
    () => listeners = listeners.filterNot(_ == listener)
  
  def start(): Unit =
    if !running then
      running = true
      lastTickTime = System.nanoTime()
      startAnimationTimer()
  
  def stop(): Unit =
    running = false
    animationTimer.foreach(_.stop())
    animationTimer = None
  
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
    if running then
      lastTickTime = System.nanoTime()
  
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
  
  private def notifyListeners(): Unit =
    listeners.foreach(_(currentState))
  
  private def startAnimationTimer(): Unit =
    animationTimer = Some(AnimationTimer(t => gameLoop(t)))
    animationTimer.foreach(_.start())
  
  private def gameLoop(now: Long): Unit =
    if running && currentState.isPlaying then
      val elapsedMs = (now - lastTickTime) / 1_000_000
      if elapsedMs >= currentState.dropInterval then
        lastTickTime = now
        Platform.runLater { () =>
          if running && currentState.isPlaying then
            handleInput(GameInput.Tick)
        }
  
  def save(path: String): Either[Exception, Unit] =
    extetris.persistence.GamePersistence.save(currentState, path)
  
  def load(path: String): Either[Exception, Unit] =
    extetris.persistence.GamePersistence.load(path).map { loadedState =>
      currentState = loadedState
      notifyListeners()
      if running && currentState.isPlaying then
        lastTickTime = System.nanoTime()
    }

object GameRunner:
  def apply(config: VariantConfig, seed: Option[Long] = None)(using ExecutionContext): GameRunner =
    val initialState = ExTetrisAPI.newGame(config, seed)
    new GameRunner(initialState)
  
  def fromState(state: GameState)(using ExecutionContext): GameRunner =
    new GameRunner(state)