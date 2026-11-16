package extetris.pieces

import scalafx.scene.paint.Color

final case class PieceColor(r: Double, g: Double, b: Double, a: Double = 1.0):
  def toScalafxColor: Color = Color(r, g, b, a)
  def toHex: String =
    val ri = (r * 255).toInt
    val gi = (g * 255).toInt
    val bi = (b * 255).toInt
    val ai = (a * 255).toInt
    f"#$ri%02X$gi%02X$bi%02X$ai%02X"

object PieceColor:
  val StandardColors: Map[PieceId, PieceColor] = Map(
    PieceId.I -> PieceColor(0.0, 1.0, 1.0),     // Cyan
    PieceId.O -> PieceColor(1.0, 1.0, 0.0),     // Yellow
    PieceId.T -> PieceColor(0.5, 0.0, 0.5),     // Purple
    PieceId.S -> PieceColor(0.0, 1.0, 0.0),     // Green
    PieceId.Z -> PieceColor(1.0, 0.0, 0.0),     // Red
    PieceId.J -> PieceColor(0.0, 0.0, 1.0),     // Blue
    PieceId.L -> PieceColor(1.0, 0.65, 0.0),    // Orange
    PieceId.Single -> PieceColor(0.5, 0.5, 0.5), // Gray
    PieceId.L3 -> PieceColor(0.0, 0.5, 1.0),    // Light Blue
    PieceId.Plus -> PieceColor(1.0, 0.0, 1.0)   // Magenta
  )
  
  def forPieceId(id: PieceId): PieceColor = StandardColors.getOrElse(id, PieceColor(1.0, 1.0, 1.0))