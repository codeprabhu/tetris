package extetris.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.pieces.{PieceId, PieceColor}

class CellTest extends AnyFunSuite with Matchers:
  
  test("Empty cell properties"):
    Cell.empty.isEmpty shouldBe true
    Cell.empty.isFilled shouldBe false
    Cell.empty.pieceId shouldBe None
    Cell.empty.color shouldBe None
  
  test("Filled cell properties"):
    val cell = Cell.filled(PieceId.I, PieceColor(0.0, 1.0, 1.0))
    cell.isEmpty shouldBe false
    cell.isFilled shouldBe true
    cell.pieceId shouldBe Some(PieceId.I)
    cell.color shouldBe Some(PieceColor(0.0, 1.0, 1.0))
  
  test("Cell pattern matching"):
    Cell.empty match
      case Cell.Empty => succeed
      case _ => fail("Should match Empty")
    
    Cell.filled(PieceId.O, PieceColor(1.0, 1.0, 0.0)) match
      case Cell.Filled(PieceId.O, color) => color shouldBe PieceColor(1.0, 1.0, 0.0)
      case _ => fail("Should match Filled")