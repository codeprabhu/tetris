package extetris.pieces

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.config.ValidationError

class PieceRegistryTest extends AnyFunSuite with Matchers:
  
  test("Default registry contains all standard pieces"):
    val registry = PieceRegistry.default
    registry.enabledIds should contain allElementsOf PieceId.standardIds
    registry.enabledIds should not contain PieceId.Single
    registry.definitions.size shouldBe 10 // 7 standard + 3 custom
  
  test("Get piece definition"):
    val registry = PieceRegistry.default
    registry.get(PieceId.I).isDefined shouldBe true
    registry.get(PieceId.I).get.name shouldBe "I"
    registry.get(PieceId.Single).isDefined shouldBe true // Definition exists but not enabled
    registry.getEnabled(PieceId.Single) shouldBe None // Not enabled by default
  
  test("Get enabled piece definition"):
    val registry = PieceRegistry.default
    registry.getEnabled(PieceId.I).isDefined shouldBe true
    registry.getEnabled(PieceId.Single).isDefined shouldBe false
  
  test("Enable piece"):
    val registry = PieceRegistry.default
    val enabled = registry.enable(PieceId.Single)
    enabled.isRight shouldBe true
    enabled.getOrElse(fail("Enable failed")).enabledIds should contain(PieceId.Single)
  
  test("Enable unknown piece fails"):
    val registry = PieceRegistry.default
    val result = registry.enable(PieceId("Unknown"))
    result.isLeft shouldBe true
    result.swap.foreach(_ shouldBe a[ValidationError])
  
  test("Disable piece"):
    val registry = PieceRegistry.default
    val disabled = registry.disable(PieceId.I)
    disabled.enabledIds should not contain PieceId.I
    disabled.definitions should contain key (PieceId.I) // Definition still exists
  
  test("Add custom definition"):
    val registry = PieceRegistry.empty
    val custom = PieceDefinition.custom(
      PieceId("Custom"), "Custom", PieceColor(1, 0, 0),
      Set(extetris.model.Position(0, 0)), extetris.pieces.RotationBehavior.None
    )
    val withCustom = registry.withDefinition(custom)
    withCustom.definitions should contain key (PieceId("Custom"))
    withCustom.enabledIds should contain (PieceId("Custom"))
  
  test("Remove definition"):
    val registry = PieceRegistry.default
    val withoutI = registry.withoutDefinition(PieceId.I)
    withoutI.definitions should not contain key (PieceId.I)
    withoutI.enabledIds should not contain PieceId.I
  
  test("Registry from definitions"):
    val defs = Seq(StandardPieces.I, StandardPieces.O, StandardPieces.T)
    val registry = PieceRegistry.fromDefinitions(defs)
    registry.definitions.size shouldBe 3
    registry.enabledIds.size shouldBe 3