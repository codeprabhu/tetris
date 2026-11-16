package extetris.board

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.model.{Position, Cell}
import extetris.pieces.{Piece, PieceDefinition, PieceId, PieceColor, StandardPieces}
import extetris.config.VariantConfig

class BoardTest extends AnyFunSuite with Matchers:
  
  test("Empty board creation"):
    val board = Board.empty(10, 20)
    board.width shouldBe 10
    board.height shouldBe 20
    board.isEmpty shouldBe true
    board.isFull shouldBe false
  
  test("Cell access within bounds"):
    val board = Board.empty(10, 20)
    board.cellAt(0, 0) shouldBe Some(Cell.empty)
    board.cellAt(9, 19) shouldBe Some(Cell.empty)
    board.cellAt(Position(5, 5)) shouldBe Some(Cell.empty)
  
  test("Cell access out of bounds"):
    val board = Board.empty(10, 20)
    board.cellAt(-1, 0) shouldBe None
    board.cellAt(10, 0) shouldBe None
    board.cellAt(0, 20) shouldBe None
    board.cellAt(Position(-1, -1)) shouldBe None
  
  test("Piece placement"):
    val board = Board.empty(10, 20)
    val piece = Piece.spawn(StandardPieces.O, 10)
    val newBoard = board.place(piece)
    newBoard.cellAt(4, 0) shouldBe Some(Cell.filled(PieceId.O, PieceColor.forPieceId(PieceId.O)))
    newBoard.cellAt(5, 0) shouldBe Some(Cell.filled(PieceId.O, PieceColor.forPieceId(PieceId.O)))
    newBoard.cellAt(4, 1) shouldBe Some(Cell.filled(PieceId.O, PieceColor.forPieceId(PieceId.O)))
    newBoard.cellAt(5, 1) shouldBe Some(Cell.filled(PieceId.O, PieceColor.forPieceId(PieceId.O)))
  
  test("Piece placement does not modify original board"):
    val board = Board.empty(10, 20)
    val piece = Piece.spawn(StandardPieces.O, 10)
    val newBoard = board.place(piece)
    board.isEmpty shouldBe true
    newBoard.isEmpty shouldBe false
  
  test("Can place piece on empty board"):
    val board = Board.empty(10, 20)
    val piece = Piece.spawn(StandardPieces.I, 10)
    board.canPlace(piece) shouldBe true
  
  test("Cannot place piece overlapping occupied cells"):
    val board = Board.empty(10, 20)
    val piece1 = Piece.spawn(StandardPieces.O, 10)
    val boardWithPiece = board.place(piece1)
    val piece2 = Piece.spawn(StandardPieces.O, 10).moveTo(Position(4, 0))
    boardWithPiece.canPlace(piece2) shouldBe false
  
  test("Cannot place piece out of bounds horizontally"):
    val board = Board.empty(10, 20)
    val piece = Piece.spawn(StandardPieces.I, 10).moveTo(Position(-1, 0))
    board.canPlace(piece) shouldBe false
  
  test("Cannot place piece out of bounds vertically (below)"):
    val board = Board.empty(10, 20)
    val piece = Piece.spawn(StandardPieces.O, 10).moveTo(Position(0, 19))
    board.canPlace(piece) shouldBe false
  
  test("Can place piece partially above board (spawn area)"):
    val board = Board.empty(10, 20)
    val piece = Piece.spawn(StandardPieces.I, 10).moveTo(Position(3, -2))
    board.canPlace(piece) shouldBe true
  
  test("Line clearing - single line"):
    val board = Board.empty(10, 5)
    // Fill bottom row
    var filledBoard = board
    for x <- 0 until 10 do
      filledBoard = filledBoard.place(
        extetris.pieces.Piece(
          StandardPieces.O,
          extetris.pieces.PieceRotation.fromCells(Set(Position(0, 0))),
          Position(x, 4)
        )
      )
    val (clearedBoard, linesCleared) = filledBoard.clearLines
    linesCleared shouldBe 1
    clearedBoard.cellAt(0, 4) shouldBe Some(Cell.empty)
  
  test("Line clearing - multiple lines"):
    val board = Board.empty(10, 5)
    var filledBoard = board
    for y <- 3 to 4 do
      for x <- 0 until 10 do
        filledBoard = filledBoard.place(
          extetris.pieces.Piece(
            StandardPieces.O,
            extetris.pieces.PieceRotation.fromCells(Set(Position(0, 0))),
            Position(x, y)
          )
        )
    val (clearedBoard, linesCleared) = filledBoard.clearLines
    linesCleared shouldBe 2
    clearedBoard.isEmpty shouldBe true
  
  test("Line clearing - non-adjacent lines"):
    val board = Board.empty(10, 20)
    var filledBoard = board
    // Fill rows 5 and 8 completely
    for x <- 0 until 10 do
      filledBoard = filledBoard.place(
        extetris.pieces.Piece(
          StandardPieces.O,
          extetris.pieces.PieceRotation.fromCells(Set(Position(0, 0))),
          Position(x, 5)
        )
      ).place(
        extetris.pieces.Piece(
          StandardPieces.O,
          extetris.pieces.PieceRotation.fromCells(Set(Position(0, 0))),
          Position(x, 8)
        )
      )
    // Add some blocks in row 3 (above cleared rows) that should fall down
    for x <- 0 until 5 do
      filledBoard = filledBoard.place(
        extetris.pieces.Piece(
          StandardPieces.O,
          extetris.pieces.PieceRotation.fromCells(Set(Position(0, 0))),
          Position(x, 3)
        )
      )
    val (clearedBoard, linesCleared) = filledBoard.clearLines
    linesCleared shouldBe 2
    // Row 3 should fall to row 5 (two rows cleared below it: rows 5 and 8)
    clearedBoard.cellAt(0, 5) shouldBe Some(Cell.filled(PieceId.O, PieceColor.forPieceId(PieceId.O)))
  
  test("Board heights calculation"):
    val board = Board.empty(10, 20)
    var filledBoard = board
    for y <- 18 to 19 do
      for x <- 0 until 5 do
        filledBoard = filledBoard.place(
          extetris.pieces.Piece(
            StandardPieces.O,
            extetris.pieces.PieceRotation.fromCells(Set(Position(0, 0))),
            Position(x, y)
          )
        )
    filledBoard.heights(0) shouldBe 2
    filledBoard.heights(4) shouldBe 2
    filledBoard.heights(5) shouldBe 0
  
  test("Board holes calculation"):
    val board = Board.empty(10, 10)
    var filledBoard = board
    // Create a hole at (5, 5) with blocks above and below
    for y <- 0 to 4 do
      filledBoard = filledBoard.place(
        extetris.pieces.Piece(
          StandardPieces.O,
          extetris.pieces.PieceRotation.fromCells(Set(Position(0, 0))),
          Position(5, y)
        )
      )
    for y <- 6 to 9 do
      filledBoard = filledBoard.place(
        extetris.pieces.Piece(
          StandardPieces.O,
          extetris.pieces.PieceRotation.fromCells(Set(Position(0, 0))),
          Position(5, y)
        )
      )
    filledBoard.holes shouldBe 1
  
  test("Board bumpiness calculation"):
    val board = Board.empty(10, 10)
    var filledBoard = board
    for y <- 8 to 9 do filledBoard = filledBoard.place(
      extetris.pieces.Piece(StandardPieces.O, extetris.pieces.PieceRotation.fromCells(Set(Position(0,0))), Position(0, y))
    )
    for y <- 5 to 9 do filledBoard = filledBoard.place(
      extetris.pieces.Piece(StandardPieces.O, extetris.pieces.PieceRotation.fromCells(Set(Position(0,0))), Position(1, y))
    )
    // Column 0: height 2, Column 1: height 5, others: 0
    // Bumpiness = |2-5| + |5-0| + |0-0|*7 = 3 + 5 = 8
    filledBoard.bumpiness shouldBe 8