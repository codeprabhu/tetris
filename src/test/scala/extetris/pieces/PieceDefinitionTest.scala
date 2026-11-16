package extetris.pieces

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.model.Position

class PieceDefinitionTest extends AnyFunSuite with Matchers:
  
  test("Standard piece definition - I piece"):
    val defn = StandardPieces.I
    defn.id shouldBe PieceId.I
    defn.name shouldBe "I"
    defn.behavior shouldBe RotationBehavior.Limited(2)
    defn.rotationCount shouldBe 2
  
  test("Standard piece definition - O piece"):
    val defn = StandardPieces.O
    defn.id shouldBe PieceId.O
    defn.behavior shouldBe RotationBehavior.None
    defn.rotationCount shouldBe 1
  
  test("Standard piece definition - T piece"):
    val defn = StandardPieces.T
    defn.id shouldBe PieceId.T
    defn.behavior shouldBe RotationBehavior.Full
    defn.rotationCount shouldBe 4
  
  test("Custom piece definition - Single"):
    val defn = CustomPieces.Single
    defn.id shouldBe PieceId.Single
    defn.name shouldBe "Single"
    defn.behavior shouldBe RotationBehavior.None
    defn.rotationCount shouldBe 1
    defn.initialRotation.cells.size shouldBe 1
  
  test("Custom piece definition - L3"):
    val defn = CustomPieces.L3
    defn.id shouldBe PieceId.L3
    defn.initialRotation.cells.size shouldBe 3
  
  test("Custom piece definition - Plus"):
    val defn = CustomPieces.Plus
    defn.id shouldBe PieceId.Plus
    defn.initialRotation.cells.size shouldBe 5
  
  test("Next rotation cycles"):
    val defn = StandardPieces.T
    val rot0 = defn.initialRotation
    val rot1 = defn.nextRotation(rot0)
    val rot2 = defn.nextRotation(rot1)
    val rot3 = defn.nextRotation(rot2)
    val rot4 = defn.nextRotation(rot3)
    rot4 shouldBe rot0
  
  test("Previous rotation cycles"):
    val defn = StandardPieces.T
    val rot0 = defn.initialRotation
    val rot3 = defn.prevRotation(rot0)
    val rot2 = defn.prevRotation(rot3)
    val rot1 = defn.prevRotation(rot2)
    val rot0Again = defn.prevRotation(rot1)
    rot0Again shouldBe rot0
  
  test("Custom piece with limited rotation"):
    val defn = PieceDefinition.custom(
      PieceId("Test"), "Test", PieceColor(1, 0, 0),
      Set(Position(0, 0), Position(1, 0), Position(2, 0)),
      RotationBehavior.Limited(2)
    )
    defn.rotationCount shouldBe 2
  
  test("Custom piece with no rotation"):
    val defn = PieceDefinition.custom(
      PieceId("Test"), "Test", PieceColor(1, 0, 0),
      Set(Position(0, 0), Position(1, 0)),
      RotationBehavior.None
    )
    defn.rotationCount shouldBe 1
    defn.nextRotation(defn.initialRotation) shouldBe defn.initialRotation