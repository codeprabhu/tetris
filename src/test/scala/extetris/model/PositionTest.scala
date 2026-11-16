package extetris.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PositionTest extends AnyFunSuite with Matchers:
  
  test("Position addition"):
    Position(1, 2) + Position(3, 4) shouldBe Position(4, 6)
  
  test("Position subtraction"):
    Position(5, 7) - Position(2, 3) shouldBe Position(3, 4)
  
  test("Position scalar multiplication"):
    Position(2, 3) * 4 shouldBe Position(8, 12)
  
  test("Position translation"):
    Position(1, 1).translate(2, 3) shouldBe Position(3, 4)
  
  test("Position rotation CW around origin"):
    Position(1, 0).rotateCW() shouldBe Position(0, 1)
    Position(0, 1).rotateCW() shouldBe Position(-1, 0)
    Position(-1, 0).rotateCW() shouldBe Position(0, -1)
    Position(0, -1).rotateCW() shouldBe Position(1, 0)
  
  test("Position rotation CCW around origin"):
    Position(1, 0).rotateCCW() shouldBe Position(0, -1)
    Position(0, 1).rotateCCW() shouldBe Position(1, 0)
  
  test("Position rotation around custom origin"):
    Position(2, 2).rotateCW(Position(1, 1)) shouldBe Position(0, 2)
  
  test("Position inBounds"):
    Position(0, 0).inBounds(10, 20) shouldBe true
    Position(9, 19).inBounds(10, 20) shouldBe true
    Position(10, 0).inBounds(10, 20) shouldBe false
    Position(0, 20).inBounds(10, 20) shouldBe false
    Position(-1, 0).inBounds(10, 20) shouldBe false