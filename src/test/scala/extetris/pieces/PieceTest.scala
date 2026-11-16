package extetris.pieces

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.model.Position
import extetris.config.VariantConfig

class PieceTest extends AnyFunSuite with Matchers:
  
  test("Standard piece spawn position"):
    val piece = Piece.spawn(StandardPieces.I, 10)
    piece.position.x shouldBe 3 // (10 - 4) / 2 = 3
    piece.position.y shouldBe 0
  
  test("O piece spawn position"):
    val piece = Piece.spawn(StandardPieces.O, 10)
    piece.position.x shouldBe 4 // (10 - 2) / 2 = 4
    piece.position.y shouldBe 0
  
  test("Piece movement"):
    val piece = Piece.spawn(StandardPieces.I, 10)
    val moved = piece.move(1, 2)
    moved.position shouldBe Position(4, 2)
    moved.definition shouldBe piece.definition
    moved.rotation shouldBe piece.rotation
  
  test("Piece rotation CW"):
    val piece = Piece.spawn(StandardPieces.T, 10)
    val rotated = piece.rotateCW
    rotated.rotation should not be piece.rotation
    rotated.definition shouldBe piece.definition
    rotated.position shouldBe piece.position
  
  test("Piece rotation CCW"):
    val piece = Piece.spawn(StandardPieces.T, 10)
    val rotated = piece.rotateCCW
    rotated.rotation should not be piece.rotation
    rotated.definition shouldBe piece.definition
    rotated.position shouldBe piece.position
  
  test("Piece cells calculation"):
    val piece = Piece.spawn(StandardPieces.O, 10)
    piece.cells shouldBe Set(
      Position(4, 0), Position(5, 0),
      Position(4, 1), Position(5, 1)
    )
  
  test("I piece rotation states"):
    val piece = Piece.spawn(StandardPieces.I, 10)
    val rot0 = piece.rotation
    val rot1 = piece.rotateCW.rotation
    val rot2 = piece.rotateCW.rotateCW.rotation
    val rot3 = piece.rotateCW.rotateCW.rotateCW.rotation
    
    rot0.cells.map(_.x).max - rot0.cells.map(_.x).min shouldBe 3 // Horizontal
    rot1.cells.map(_.y).max - rot1.cells.map(_.y).min shouldBe 3 // Vertical
    rot2.cells.map(_.x).max - rot2.cells.map(_.x).min shouldBe 3 // Horizontal
    rot3.cells.map(_.y).max - rot3.cells.map(_.y).min shouldBe 3 // Vertical
  
  test("O piece has no rotation"):
    val piece = Piece.spawn(StandardPieces.O, 10)
    piece.rotateCW.rotation.cells shouldBe piece.rotation.cells
    piece.rotateCCW.rotation.cells shouldBe piece.rotation.cells
  
  test("Custom piece - Single"):
    val piece = Piece.spawn(CustomPieces.Single, 10)
    piece.cells shouldBe Set(Position(4, 0)) // Center of 10-wide board
    piece.rotateCW.rotation.cells shouldBe piece.rotation.cells
  
  test("Custom piece - L3"):
    val piece = Piece.spawn(CustomPieces.L3, 10)
    piece.cells.size shouldBe 3
    piece.definition.id shouldBe PieceId.L3
  
  test("Custom piece - Plus"):
    val piece = Piece.spawn(CustomPieces.Plus, 10)
    piece.cells.size shouldBe 5
    piece.definition.id shouldBe PieceId.Plus
  
  test("Piece bounding box"):
    val piece = Piece.spawn(StandardPieces.O, 10)
    val (x, y, w, h) = piece.boundingBox
    x shouldBe 4
    y shouldBe 0
    w shouldBe 2
    h shouldBe 2
  
  test("Piece ID and color"):
    val piece = Piece.spawn(StandardPieces.T, 10)
    piece.id shouldBe PieceId.T
    piece.color shouldBe PieceColor.forPieceId(PieceId.T)
    piece.name shouldBe "T"