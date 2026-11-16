package extetris.pieces

import extetris.model.Position

enum RotationBehavior:
  case Full
  case Limited(states: Int)
  case None

final case class PieceRotation(
  cells: Set[Position],
  origin: Position = Position(0, 0)
):
  def rotateCW: PieceRotation =
    PieceRotation(cells.map(_.rotateCW(origin)), origin)
  
  def rotateCCW: PieceRotation =
    PieceRotation(cells.map(_.rotateCCW(origin)), origin)
  
  def translate(dx: Int, dy: Int): PieceRotation =
    PieceRotation(cells.map(_.translate(dx, dy)), origin.translate(dx, dy))
  
  def width: Int =
    if cells.isEmpty then 0 else cells.map(_.x).max - cells.map(_.x).min + 1
  
  def height: Int =
    if cells.isEmpty then 0 else cells.map(_.y).max - cells.map(_.y).min + 1
  
  def boundingBox: (Int, Int, Int, Int) =
    if cells.isEmpty then (0, 0, 0, 0)
    else
      val minX = cells.map(_.x).min
      val maxX = cells.map(_.x).max
      val minY = cells.map(_.y).min
      val maxY = cells.map(_.y).max
      (minX, minY, maxX - minX + 1, maxY - minY + 1)

object PieceRotation:
  def fromCells(cells: Set[Position]): PieceRotation =
    val minX = cells.map(_.x).min
    val minY = cells.map(_.y).min
    val normalized = cells.map(p => Position(p.x - minX, p.y - minY))
    val origin = Position(
      normalized.map(_.x).max / 2,
      normalized.map(_.y).max / 2
    )
    PieceRotation(normalized, origin)
  
  def fromCellsWithOrigin(cells: Set[Position], origin: Position): PieceRotation =
    PieceRotation(cells, origin)