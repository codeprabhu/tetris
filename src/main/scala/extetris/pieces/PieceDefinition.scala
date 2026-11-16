package extetris.pieces

import extetris.model.Position

sealed trait PieceDefinition:
  def id: PieceId
  def name: String
  def color: PieceColor
  def rotations: Vector[PieceRotation]
  def behavior: RotationBehavior
  
  def rotationCount: Int = rotations.length
  def initialRotation: PieceRotation = rotations.head
  def nextRotation(current: PieceRotation): PieceRotation =
    val idx = rotations.indexOf(current)
    if idx >= 0 then rotations((idx + 1) % rotations.length) else rotations.head
  def prevRotation(current: PieceRotation): PieceRotation =
    val idx = rotations.indexOf(current)
    if idx >= 0 then rotations((idx - 1 + rotations.length) % rotations.length) else rotations.head

object PieceDefinition:
  def standard(id: PieceId, name: String, color: PieceColor, cells: Set[Position], behavior: RotationBehavior = RotationBehavior.Full): PieceDefinition =
    val rotations = generateRotations(cells, behavior)
    StandardPiece(id, name, color, rotations, behavior)
  
  def custom(id: PieceId, name: String, color: PieceColor, cells: Set[Position], behavior: RotationBehavior = RotationBehavior.Full): PieceDefinition =
    val rotations = generateRotations(cells, behavior)
    CustomPiece(id, name, color, rotations, behavior)
  
  private def generateRotations(cells: Set[Position], behavior: RotationBehavior): Vector[PieceRotation] =
    behavior match
      case RotationBehavior.Full =>
        val rot0 = PieceRotation.fromCells(cells)
        val rot1 = rot0.rotateCW
        val rot2 = rot1.rotateCW
        val rot3 = rot2.rotateCW
        Vector(rot0, rot1, rot2, rot3).distinct
      case RotationBehavior.Limited(states) =>
        val rot0 = PieceRotation.fromCells(cells)
        val rotations = (1 until states).foldLeft(Vector(rot0)) { (acc, _) =>
          acc :+ acc.last.rotateCW
        }
        rotations.distinct
      case RotationBehavior.None =>
        Vector(PieceRotation.fromCells(cells))

final case class StandardPiece(
  id: PieceId,
  name: String,
  color: PieceColor,
  rotations: Vector[PieceRotation],
  behavior: RotationBehavior
) extends PieceDefinition

final case class CustomPiece(
  id: PieceId,
  name: String,
  color: PieceColor,
  rotations: Vector[PieceRotation],
  behavior: RotationBehavior
) extends PieceDefinition