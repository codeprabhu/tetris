package extetris.model

final case class Position(x: Int, y: Int):
  def +(other: Position): Position = Position(x + other.x, y + other.y)
  def -(other: Position): Position = Position(x - other.x, y - other.y)
  def *(scalar: Int): Position = Position(x * scalar, y * scalar)
  
  def translate(dx: Int, dy: Int): Position = Position(x + dx, y + dy)
  
  def rotateCW(origin: Position = Position(0, 0)): Position =
    val relX = x - origin.x
    val relY = y - origin.y
    Position(origin.x - relY, origin.y + relX)
  
  def rotateCCW(origin: Position = Position(0, 0)): Position =
    val relX = x - origin.x
    val relY = y - origin.y
    Position(origin.x + relY, origin.y - relX)
  
  def inBounds(width: Int, height: Int): Boolean =
    x >= 0 && x < width && y >= 0 && y < height