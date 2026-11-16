package extetris.examples

import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig, GenerationStrategy}
import extetris.pieces.{PieceId, PieceDefinition, PieceColor, StandardPieces, CustomPieces}
import extetris.model.Position
import extetris.api.ExTetrisAPI

object ExampleVariants:
  
  // Classic ExTetris - Standard Tetris experience
  val classic: VariantConfig = VariantConfig.classic
  
  // Extended ExTetris - All pieces with weighted generation
  val extended: VariantConfig = VariantConfig.extended
  
  // Mini ExTetris - Small board, fast gameplay
  val mini: VariantConfig = VariantConfig.builder("Mini ExTetris")
    .boardSize(6, 12)
    .enabledPieces(Set(PieceId.I, PieceId.O, PieceId.T, PieceId.Single, PieceId.L3))
    .pieceGeneration(PieceGenerationConfig.uniform())
    .scoring(ScoringConfig(singleLine = 50, doubleLine = 150, tripleLine = 300, tetrisLine = 500))
    .levelConfig(LevelConfig(linesPerLevel = 5, initialDropInterval = 600, minDropInterval = 50, dropIntervalDecrease = 40))
    .initialLevel(1)
    .allowHold(true)
    .allowHardDrop(true)
    .previewCount(2)
    .build()
  
  // Giant ExTetris - Large board, all pieces
  val giant: VariantConfig = VariantConfig.builder("Giant ExTetris")
    .boardSize(20, 40)
    .enabledPieces(PieceId.allBuiltInIds)
    .pieceGeneration(PieceGenerationConfig.weighted(
      Map(
        PieceId.I -> 5, PieceId.O -> 3, PieceId.T -> 4,
        PieceId.S -> 3, PieceId.Z -> 3, PieceId.J -> 4, PieceId.L -> 4,
        PieceId.Single -> 2, PieceId.L3 -> 3, PieceId.Plus -> 2
      )
    ))
    .scoring(ScoringConfig(singleLine = 200, doubleLine = 600, tripleLine = 1000, tetrisLine = 1600))
    .levelConfig(LevelConfig(linesPerLevel = 20, initialDropInterval = 1200, minDropInterval = 100, dropIntervalDecrease = 30))
    .initialLevel(1)
    .allowHold(true)
    .allowHardDrop(true)
    .previewCount(5)
    .build()
  
  // Challenge ExTetris - Only difficult pieces, no hold, fast
  val challenge: VariantConfig = VariantConfig.builder("Challenge ExTetris")
    .boardSize(10, 20)
    .enabledPieces(Set(PieceId.S, PieceId.Z, PieceId.Plus))
    .pieceGeneration(PieceGenerationConfig.weighted(
      Map(PieceId.S -> 2, PieceId.Z -> 2, PieceId.Plus -> 1)
    ))
    .scoring(ScoringConfig(singleLine = 200, doubleLine = 600, tripleLine = 1200, tetrisLine = 2000))
    .levelConfig(LevelConfig(linesPerLevel = 5, initialDropInterval = 500, minDropInterval = 30, dropIntervalDecrease = 50))
    .initialLevel(5)
    .allowHold(false)
    .allowHardDrop(true)
    .previewCount(1)
    .build()
  
  // Custom piece example - Demonstrates defining a new piece using Plus (built-in)
  val customPieceVariant: VariantConfig =
    VariantConfig.builder("Custom Piece Demo")
      .boardSize(10, 20)
      .enabledPieces(PieceId.standardIds + PieceId.Plus)
      .pieceGeneration(PieceGenerationConfig.weighted(
        Map(
          PieceId.I -> 2, PieceId.O -> 2, PieceId.T -> 2,
          PieceId.S -> 2, PieceId.Z -> 2, PieceId.J -> 2, PieceId.L -> 2,
          PieceId.Plus -> 3
        )
      ))
      .scoring(ScoringConfig.default)
      .levelConfig(LevelConfig.default)
      .initialLevel(1)
      .allowHold(true)
      .allowHardDrop(true)
      .previewCount(3)
      .build()
  
  // All example variants
  val all: Map[String, VariantConfig] = Map(
    "Classic" -> classic,
    "Extended" -> extended,
    "Mini" -> mini,
    "Giant" -> giant,
    "Challenge" -> challenge,
    "CustomPieceDemo" -> customPieceVariant
  )
  
  def runExample(name: String): Unit =
    all.get(name).foreach { config =>
      println(s"Starting $name variant...")
      val state = ExTetrisAPI.newGame(config, Some(12345L))
      println(s"Initial state: ${state.status}")
      println(s"Board: ${config.boardWidth}x${config.boardHeight}")
      println(s"Enabled pieces: ${config.enabledPieces.map(_.value).mkString(", ")}")
    }