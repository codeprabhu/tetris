package extetris.examples

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.config.VariantConfig
import extetris.api.ExTetrisAPI
import extetris.pieces.PieceId

class ExampleVariantsTest extends AnyFunSuite with Matchers:
  
  test("Classic variant is valid"):
    ExampleVariants.classic.validate.isRight shouldBe true
    ExampleVariants.classic.enabledPieces shouldBe PieceId.standardIds
    ExampleVariants.classic.allowHold shouldBe false
    ExampleVariants.classic.allowHardDrop shouldBe false
  
  test("Extended variant is valid"):
    ExampleVariants.extended.validate.isRight shouldBe true
    ExampleVariants.extended.enabledPieces shouldBe PieceId.allBuiltInIds
    ExampleVariants.extended.allowHold shouldBe true
    ExampleVariants.extended.allowHardDrop shouldBe true
    ExampleVariants.extended.previewCount shouldBe 3
  
  test("Mini variant is valid"):
    ExampleVariants.mini.validate.isRight shouldBe true
    ExampleVariants.mini.boardWidth shouldBe 6
    ExampleVariants.mini.boardHeight shouldBe 12
    ExampleVariants.mini.enabledPieces should contain(PieceId.Single)
    ExampleVariants.mini.enabledPieces should contain(PieceId.L3)
  
  test("Giant variant is valid"):
    ExampleVariants.giant.validate.isRight shouldBe true
    ExampleVariants.giant.boardWidth shouldBe 20
    ExampleVariants.giant.boardHeight shouldBe 40
    ExampleVariants.giant.enabledPieces shouldBe PieceId.allBuiltInIds
  
  test("Challenge variant is valid"):
    ExampleVariants.challenge.validate.isRight shouldBe true
    ExampleVariants.challenge.enabledPieces shouldBe Set(PieceId.S, PieceId.Z, PieceId.Plus)
    ExampleVariants.challenge.allowHold shouldBe false
    ExampleVariants.challenge.initialLevel shouldBe 5
  
  test("Custom piece variant is valid"):
    ExampleVariants.customPieceVariant.validate.isRight shouldBe true
    ExampleVariants.customPieceVariant.enabledPieces should contain(PieceId.Plus)
  
  test("All variants can create games"):
    ExampleVariants.all.values.foreach { config =>
      val state = ExTetrisAPI.newGame(config, Some(12345L))
      state.status shouldBe extetris.model.GameStatus.Playing
      state.currentPiece.isDefined shouldBe true
      state.board.width shouldBe config.boardWidth
      state.board.height shouldBe config.boardHeight
    }
  
  test("Run example produces output"):
    // Just verify it doesn't throw
    ExampleVariants.runExample("Classic")
    ExampleVariants.runExample("Extended")
    ExampleVariants.runExample("Mini")