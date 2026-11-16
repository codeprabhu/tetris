package extetris.config

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.pieces.PieceId

class ConfigTest extends AnyFunSuite with Matchers:
  
  test("VariantConfig default validation"):
    val config = VariantConfig.default
    config.validate.isRight shouldBe true
  
  test("VariantConfig classic validation"):
    val config = VariantConfig.classic
    config.validate.isRight shouldBe true
    config.allowHold shouldBe false
    config.allowHardDrop shouldBe false
  
  test("VariantConfig extended validation"):
    val config = VariantConfig.extended
    config.validate.isRight shouldBe true
    config.enabledPieces should contain allElementsOf PieceId.allBuiltInIds
  
  test("Invalid board width too small"):
    val config = VariantConfig.default.withBoardSize(3, 20)
    config.validate.isLeft shouldBe true
  
  test("Invalid board width too large"):
    val config = VariantConfig.default.withBoardSize(51, 20)
    config.validate.isLeft shouldBe true
  
  test("Invalid board height too small"):
    val config = VariantConfig.default.withBoardSize(10, 3)
    config.validate.isLeft shouldBe true
  
  test("No enabled pieces"):
    val config = VariantConfig.default.withEnabledPieces(Set.empty)
    config.validate.isLeft shouldBe true
  
  test("Negative weight validation"):
    val config = VariantConfig.default.withGeneration(
      PieceGenerationConfig(weights = Map(PieceId.I -> -1))
    )
    config.validate.isLeft shouldBe true
  
  test("Initial level validation"):
    val config = VariantConfig.default.withInitialLevel(0)
    config.validate.isLeft shouldBe true
  
  test("Initial level exceeds max"):
    val config = VariantConfig.default
      .withLevelConfig(LevelConfig(maxLevel = 10))
      .withInitialLevel(15)
    config.validate.isLeft shouldBe true
  
  test("Preview count validation"):
    val config = VariantConfig.default.withPreviewCount(-1)
    config.validate.isLeft shouldBe true
    
    val config2 = VariantConfig.default.withPreviewCount(6)
    config2.validate.isLeft shouldBe true
  
  test("ScoringConfig score calculation"):
    val scoring = ScoringConfig.default
    scoring.scoreForLines(1, 1) shouldBe 100
    scoring.scoreForLines(2, 1) shouldBe 300
    scoring.scoreForLines(3, 1) shouldBe 500
    scoring.scoreForLines(4, 1) shouldBe 800
    scoring.scoreForLines(1, 5) shouldBe 500 // With multiplier
    scoring.softDropScore(5) shouldBe 5
    scoring.hardDropScore(10) shouldBe 20
  
  test("Classic scoring"):
    val scoring = ScoringConfig.classic
    scoring.scoreForLines(1, 1) shouldBe 40
    scoring.scoreForLines(4, 1) shouldBe 1200
  
  test("LevelConfig drop interval"):
    val levels = LevelConfig.default
    levels.dropIntervalForLevel(1) shouldBe 1000
    levels.dropIntervalForLevel(2) shouldBe 950
    levels.dropIntervalForLevel(20) shouldBe 50 // min
    levels.dropIntervalForLevel(30) shouldBe 50 // clamped to maxLevel
  
  test("LevelConfig level for lines"):
    val levels = LevelConfig.default
    levels.levelForLines(0) shouldBe 1
    levels.levelForLines(9) shouldBe 1
    levels.levelForLines(10) shouldBe 2
    levels.levelForLines(19) shouldBe 2
    levels.levelForLines(20) shouldBe 3
  
  test("PieceGenerationConfig weightFor"):
    val gen = PieceGenerationConfig(weights = Map(PieceId.I -> 5, PieceId.O -> 3))
    gen.weightFor(PieceId.I) shouldBe 5
    gen.weightFor(PieceId.O) shouldBe 3
    gen.weightFor(PieceId.T) shouldBe 1 // Default weight
  
  test("VariantConfig builder"):
    val config = VariantConfig.builder("Test")
      .boardSize(12, 24)
      .enabledPieces(Set(PieceId.I, PieceId.O))
      .initialLevel(5)
      .allowHold(false)
      .build()
    
    config.name shouldBe "Test"
    config.boardWidth shouldBe 12
    config.boardHeight shouldBe 24
    config.enabledPieces shouldBe Set(PieceId.I, PieceId.O)
    config.initialLevel shouldBe 5
    config.allowHold shouldBe false