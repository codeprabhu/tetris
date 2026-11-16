package extetris.model

import extetris.pieces.{Piece, PieceRotation}
import extetris.board.Board

enum GameEvent:
  case PieceSpawned(piece: Piece)
  case PieceMoved(piece: Piece, from: Position, to: Position)
  case PieceRotated(piece: Piece, from: PieceRotation, to: PieceRotation)
  case PieceHardDropped(piece: Piece, from: Position, to: Position)
  case PieceLocked(piece: Piece)
  case LinesCleared(count: Int, board: Board)
  case ScoreChanged(oldScore: Long, newScore: Long)
  case LevelChanged(oldLevel: Int, newLevel: Int)
  case GameOver(finalScore: Long, finalLevel: Int, linesCleared: Int)
  case GamePaused
  case GameResumed
  case GameRestarted
  case ConfigChanged(config: extetris.config.VariantConfig)
  case HoldUsed(held: Option[extetris.pieces.PieceDefinition], current: Piece)

object GameEvent:
  def isScoringEvent(event: GameEvent): Boolean = event match
    case _: LinesCleared | _: ScoreChanged => true
    case _ => false