package extetris.pieces

import extetris.model.Position

final case class Piece(
  definition: PieceDefinition,
  rotation: PieceRotation,
  position: Position
):
  def cells: Set[Position] = rotation.cells.map(_ + position)
  
  def move(dx: Int, dy: Int): Piece =
    copy(position = position.translate(dx, dy))
  
  def moveTo(pos: Position): Piece =
    copy(position = pos)
  
  def rotateCW: Piece =
    copy(rotation = definition.nextRotation(rotation))
  
  def rotateCCW: Piece =
    copy(rotation = definition.prevRotation(rotation))
  
  def withRotation(rot: PieceRotation): Piece =
    copy(rotation = rot)
  
  def id: PieceId = definition.id
  def color: PieceColor = definition.color
  def name: String = definition.name
  
  def boundingBox: (Int, Int, Int, Int) =
    val (minX, minY, w, h) = rotation.boundingBox
    (position.x + minX, position.y + minY, w, h)

object Piece:
  def spawn(definition: PieceDefinition, boardWidth: Int): Piece =
    val rotation = definition.initialRotation
    val (_, _, w, _) = rotation.boundingBox
    val spawnX = (boardWidth - w) / 2
    Piece(definition, rotation, Position(spawnX, 0))