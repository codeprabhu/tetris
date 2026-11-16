package extetris.model

import extetris.pieces.{PieceId, PieceColor}

enum Cell:
  case Empty
  case Filled(pieceId: PieceId, color: PieceColor)

object Cell:
  def empty: Cell = Empty
  def filled(pieceId: PieceId, color: PieceColor): Cell = Filled(pieceId, color)
  
  extension (c: Cell)
    def isEmpty: Boolean = c == Empty
    def isFilled: Boolean = c.isInstanceOf[Filled]
    def pieceId: Option[PieceId] = c match
      case Filled(id, _) => Some(id)
      case _ => None
    def color: Option[PieceColor] = c match
      case Filled(_, color) => Some(color)
      case _ => None