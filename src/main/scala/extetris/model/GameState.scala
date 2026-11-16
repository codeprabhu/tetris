package extetris.model

import extetris.board.Board
import extetris.pieces.{Piece, PieceDefinition}
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig}
import extetris.generation.RandomState

enum GameStatus:
  case Playing, Paused, GameOver, NotStarted

final case class GameState(
  board: Board,
  currentPiece: Option[Piece],
  nextPiece: Option[Piece],
  heldPiece: Option[PieceDefinition],
  canHold: Boolean,
  score: Long,
  level: Int,
  linesCleared: Int,
  status: GameStatus,
  config: VariantConfig,
  randomState: RandomState,
  dropCounter: Int,
  dropInterval: Int,
  lastAction: Option[GameInput] = None
):
  def isPlaying: Boolean = status == GameStatus.Playing
  def isPaused: Boolean = status == GameStatus.Paused
  def isGameOver: Boolean = status == GameStatus.GameOver
  def isNotStarted: Boolean = status == GameStatus.NotStarted
  
  def withStatus(newStatus: GameStatus): GameState = copy(status = newStatus)
  def withBoard(newBoard: Board): GameState = copy(board = newBoard)
  def withCurrentPiece(piece: Option[Piece]): GameState = copy(currentPiece = piece)
  def withNextPiece(piece: Option[Piece]): GameState = copy(nextPiece = piece)
  def withHeldPiece(definition: Option[PieceDefinition]): GameState = copy(heldPiece = definition, canHold = false)
  def resetHold: GameState = copy(canHold = true)
  def addScore(points: Long): GameState = copy(score = score + points)
  def addLinesCleared(count: Int): GameState = copy(linesCleared = linesCleared + count)
  def setLevel(newLevel: Int): GameState = copy(level = newLevel)
  def setDropInterval(interval: Int): GameState = copy(dropInterval = interval)
  def incrementDropCounter: GameState = copy(dropCounter = dropCounter + 1)
  def resetDropCounter: GameState = copy(dropCounter = 0)
  def withRandomState(state: RandomState): GameState = copy(randomState = state)
  def withLastAction(action: GameInput): GameState = copy(lastAction = Some(action))
  def withConfig(newConfig: VariantConfig): GameState = copy(config = newConfig)

object GameState:
  def initial(config: VariantConfig, randomState: RandomState): GameState =
    val board = Board.empty(config.boardWidth, config.boardHeight)
    val dropInterval = config.levelConfig.dropIntervalForLevel(1)
    GameState(
      board = board,
      currentPiece = None,
      nextPiece = None,
      heldPiece = None,
      canHold = true,
      score = 0,
      level = config.initialLevel,
      linesCleared = 0,
      status = GameStatus.NotStarted,
      config = config,
      randomState = randomState,
      dropCounter = 0,
      dropInterval = dropInterval
    )