package extetris.pieces

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.model.Position

class PieceRotationTest extends AnyFunSuite with Matchers:
  
  test("Rotation creation from cells"):
    val cells = Set(Position(0, 0), Position(1, 0), Position(0, 1))
    val rotation = PieceRotation.fromCells(cells)
    rotation.cells should contain allElementsOf Set(Position(0, 0), Position(1, 0), Position(0, 1))
    rotation.origin shouldBe Position(0, 0) // Normalized to origin
  
  test("Rotation CW"):
    val cells = Set(Position(0, 0), Position(1, 0))
    val rotation = PieceRotation.fromCells(cells)
    val rotated = rotation.rotateCW
    rotated.cells should contain allElementsOf Set(Position(0, 0), Position(0, 1))
  
  test("Rotation CCW"):
    val cells = Set(Position(0, 0), Position(0, 1))
    val rotation = PieceRotation.fromCells(cells)
    val rotated = rotation.rotateCCW
    rotated.cells should contain allElementsOf Set(Position(0, 0), Position(1, 0))
  
  test("Rotation translation"):
    val cells = Set(Position(0, 0), Position(1, 0))
    val rotation = PieceRotation.fromCells(cells)
    val translated = rotation.translate(5, 3)
    translated.cells should contain allElementsOf Set(Position(5, 3), Position(6, 3))
    translated.origin shouldBe Position(5, 3)
  
  test("Width and height calculation"):
    val cells = Set(Position(0, 0), Position(1, 0), Position(2, 0), Position(3, 0))
    val rotation = PieceRotation.fromCells(cells)
    rotation.width shouldBe 4
    rotation.height shouldBe 1
  
  test("Bounding box"):
    val cells = Set(Position(2, 2), Position(3, 2), Position(2, 3))
    val rotation = PieceRotation.fromCells(cells)
    val (minX, minY, w, h) = rotation.boundingBox
    minX shouldBe 0
    minY shouldBe 0
    w shouldBe 2
    h shouldBe 2
  
  test("Distinct rotations for I piece"):
    val cells = Set(Position(0, 1), Position(1, 1), Position(2, 1), Position(3, 1))
    val rot0 = PieceRotation.fromCells(cells)
    val rot1 = rot0.rotateCW
    val rot2 = rot1.rotateCW
    val rot3 = rot2.rotateCW
    val rot4 = rot3.rotateCW
    
    rot0.cells should not equal rot1.cells
    rot1.cells should not equal rot2.cells
    rot2.cells should not equal rot3.cells
    rot3.cells should not equal rot0.cells // Should be different from horizontal
    rot4.cells should equal(rot0.cells) // Back to start after 4 rotations
  
  test("Distinct rotations for O piece (square)"):
    val cells = Set(Position(0, 0), Position(1, 0), Position(0, 1), Position(1, 1))
    val rot0 = PieceRotation.fromCells(cells)
    val rot1 = rot0.rotateCW
    // Square rotated 90 degrees has same shape but different absolute positions
    // Normalize both for comparison
    val norm0 = PieceRotation.fromCells(rot0.cells)
    val norm1 = PieceRotation.fromCells(rot1.cells)
    norm0.cells should equal(norm1.cells) // Square looks same after rotation