package extetris.board

import extetris.model.{Position, Cell}
import extetris.pieces.{Piece, PieceId}

final case class Board private (
  width: Int,
  height: Int,
  grid: Vector[Vector[Cell]]
):
  require(width > 0, "Board width must be positive")
  require(height > 0, "Board height must be positive")
  require(grid.length == height, "Grid height must match board height")
  require(grid.forall(_.length == width), "Grid width must match board width")
  
  def cellAt(pos: Position): Option[Cell] =
    if pos.inBounds(width, height) then Some(grid(pos.y)(pos.x)) else None
  
  def cellAt(x: Int, y: Int): Option[Cell] =
    if x >= 0 && x < width && y >= 0 && y < height then Some(grid(y)(x)) else None
  
  def isEmpty(pos: Position): Boolean =
    cellAt(pos).forall(_.isEmpty)
  
  def isOccupied(pos: Position): Boolean =
    cellAt(pos).exists(_.isFilled)
  
  def isOccupied(x: Int, y: Int): Boolean =
    cellAt(x, y).exists(_.isFilled)
  
  def canPlace(piece: Piece): Boolean =
    piece.cells.forall { pos =>
      pos.x >= 0 && pos.x < width && pos.y < height && (pos.y < 0 || isEmpty(pos))
    }
  
  def place(piece: Piece): Board =
    val newGrid = piece.cells.foldLeft(grid) { (acc, pos) =>
      if pos.inBounds(width, height) then
        acc.updated(pos.y, acc(pos.y).updated(pos.x, Cell.filled(piece.id, piece.color)))
      else acc
    }
    copy(grid = newGrid)
  
  def clearLines: (Board, Int) =
    val fullRows = grid.zipWithIndex.filter((row, _) => row.forall(_.isFilled)).map(_._2)
    if fullRows.isEmpty then (this, 0)
    else
      val newRows = grid.zipWithIndex.filterNot((_, idx) => fullRows.contains(idx)).map(_._1)
      val emptyRow = Vector.fill(width)(Cell.empty)
      val padding = Vector.fill(fullRows.length)(emptyRow)
      val updatedGrid = padding ++ newRows
      (copy(grid = updatedGrid), fullRows.length)
  
  def heights: Vector[Int] =
    (0 until width).map { x =>
      grid.indexWhere(row => row(x).isFilled) match
        case -1 => 0
        case y => height - y
    }.toVector
  
  def aggregateHeight: Int = heights.sum
  def maxHeight: Int = heights.maxOption.getOrElse(0)
  def holes: Int =
    (0 until width).map { x =>
      var foundBlock = false
      var count = 0
      for y <- 0 until height do
        if grid(y)(x).isFilled then foundBlock = true
        else if foundBlock then count += 1
      count
    }.sum
  
  def bumpiness: Int =
    if heights.length < 2 then 0
    else heights.sliding(2).collect { case Vector(a, b) => (a - b).abs }.sum
  
  def isEmpty: Boolean = grid.forall(_.forall(_.isEmpty))
  def isFull: Boolean = grid.head.forall(_.isFilled)
  
  def row(y: Int): Option[Vector[Cell]] =
    if y >= 0 && y < height then Some(grid(y)) else None
  
  def column(x: Int): Option[Vector[Cell]] =
    if x >= 0 && x < width then Some(grid.map(_(x))) else None
  
  def toVector: Vector[Vector[Cell]] = grid

object Board:
  def empty(width: Int, height: Int): Board =
    val row = Vector.fill(width)(Cell.empty)
    Board(width, height, Vector.fill(height)(row))
  
  def fromGrid(grid: Vector[Vector[Cell]]): Board =
    val height = grid.length
    val width = if height > 0 then grid.head.length else 0
    Board(width, height, grid)