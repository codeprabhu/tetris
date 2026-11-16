package extetris.config

import extetris.pieces.PieceId
import extetris.config.ValidationError

final case class VariantConfig(
  name: String,
  boardWidth: Int = 10,
  boardHeight: Int = 20,
  enabledPieces: Set[PieceId] = PieceId.standardIds,
  pieceGeneration: PieceGenerationConfig = PieceGenerationConfig.default,
  scoring: ScoringConfig = ScoringConfig.default,
  levelConfig: LevelConfig = LevelConfig.default,
  initialLevel: Int = 1,
  allowHold: Boolean = true,
  allowHardDrop: Boolean = true,
  previewCount: Int = 1,
  gravityEnabled: Boolean = true
):
  def validate: Either[ValidationError, VariantConfig] =
    for
      _ <- Either.cond(boardWidth >= 4, (), ValidationError(s"Board width must be at least 4, got $boardWidth"))
      _ <- Either.cond(boardHeight >= 4, (), ValidationError(s"Board height must be at least 4, got $boardHeight"))
      _ <- Either.cond(boardWidth <= 50, (), ValidationError(s"Board width must be at most 50, got $boardWidth"))
      _ <- Either.cond(boardHeight <= 100, (), ValidationError(s"Board height must be at most 100, got $boardHeight"))
      _ <- Either.cond(enabledPieces.nonEmpty, (), ValidationError("At least one piece must be enabled"))
      _ <- pieceGeneration.weights.filter(_._2 < 0).headOption match
        case Some((id, _)) => Left(ValidationError(s"Weight for piece '$id' must be non-negative"))
        case None => Right(())
      _ <- Either.cond(initialLevel >= 1, (), ValidationError(s"Initial level must be at least 1, got $initialLevel"))
      _ <- Either.cond(initialLevel <= levelConfig.maxLevel, (), ValidationError(s"Initial level cannot exceed max level (${levelConfig.maxLevel})"))
      _ <- Either.cond(previewCount >= 0, (), ValidationError(s"Preview count must be non-negative, got $previewCount"))
      _ <- Either.cond(previewCount <= 5, (), ValidationError(s"Preview count must be at most 5, got $previewCount"))
    yield this
  
  def withEnabledPieces(pieces: Set[PieceId]): VariantConfig =
    copy(enabledPieces = pieces)
  
  def withBoardSize(width: Int, height: Int): VariantConfig =
    copy(boardWidth = width, boardHeight = height)
  
  def withGeneration(config: PieceGenerationConfig): VariantConfig =
    copy(pieceGeneration = config)
  
  def withScoring(config: ScoringConfig): VariantConfig =
    copy(scoring = config)
  
  def withLevelConfig(config: LevelConfig): VariantConfig =
    copy(levelConfig = config)
  
  def withInitialLevel(level: Int): VariantConfig =
    copy(initialLevel = level)
  
  def withHold(enabled: Boolean): VariantConfig =
    copy(allowHold = enabled)
  
  def withAllowHold(enabled: Boolean): VariantConfig =
    copy(allowHold = enabled)
  
  def withHardDrop(enabled: Boolean): VariantConfig =
    copy(allowHardDrop = enabled)
  
  def withAllowHardDrop(enabled: Boolean): VariantConfig =
    copy(allowHardDrop = enabled)
  
  def withPreviewCount(count: Int): VariantConfig =
    copy(previewCount = count)
  
  def withGravity(enabled: Boolean): VariantConfig =
    copy(gravityEnabled = enabled)

object VariantConfig:
  val default: VariantConfig = VariantConfig("Default")
  
  def classic: VariantConfig = VariantConfig(
    name = "Classic ExTetris",
    boardWidth = 10,
    boardHeight = 20,
    enabledPieces = PieceId.standardIds,
    pieceGeneration = PieceGenerationConfig.uniform(),
    scoring = ScoringConfig.classic,
    levelConfig = LevelConfig.default,
    initialLevel = 1,
    allowHold = false,
    allowHardDrop = false,
    previewCount = 1,
    gravityEnabled = true
  )
  
  def extended: VariantConfig = VariantConfig(
    name = "Extended ExTetris",
    boardWidth = 10,
    boardHeight = 20,
    enabledPieces = PieceId.allBuiltInIds,
    pieceGeneration = PieceGenerationConfig.weighted(
      Map(
        PieceId.I -> 3, PieceId.O -> 2, PieceId.T -> 3,
        PieceId.S -> 2, PieceId.Z -> 2, PieceId.J -> 3, PieceId.L -> 3,
        PieceId.Single -> 1, PieceId.L3 -> 2, PieceId.Plus -> 1
      )
    ),
    scoring = ScoringConfig.default,
    levelConfig = LevelConfig.fast,
    initialLevel = 1,
    allowHold = true,
    allowHardDrop = true,
    previewCount = 3,
    gravityEnabled = true
  )
  
  def builder(name: String): VariantConfigBuilder = VariantConfigBuilder(name)
  
  final class VariantConfigBuilder private[VariantConfig] (name: String):
    private var width: Int = 10
    private var height: Int = 20
    private var enabled: Set[PieceId] = PieceId.standardIds
    private var generation: PieceGenerationConfig = PieceGenerationConfig.default
    private var scoring: ScoringConfig = ScoringConfig.default
    private var levels: LevelConfig = LevelConfig.default
    private var initLevel: Int = 1
    private var hold: Boolean = true
    private var hardDrop: Boolean = true
    private var preview: Int = 1
    private var gravity: Boolean = true
    
    def boardSize(width: Int, height: Int): this.type = { this.width = width; this.height = height; this }
    def enabledPieces(pieces: Set[PieceId]): this.type = { this.enabled = pieces; this }
    def pieceGeneration(config: PieceGenerationConfig): this.type = { this.generation = config; this }
    def scoring(config: ScoringConfig): this.type = { this.scoring = config; this }
    def levelConfig(config: LevelConfig): this.type = { this.levels = config; this }
    def initialLevel(level: Int): this.type = { this.initLevel = level; this }
    def allowHold(enabled: Boolean): this.type = { this.hold = enabled; this }
    def allowHardDrop(enabled: Boolean): this.type = { this.hardDrop = enabled; this }
    def previewCount(count: Int): this.type = { this.preview = count; this }
    def gravity(enabled: Boolean): this.type = { this.gravity = enabled; this }
    
    def build(): VariantConfig = VariantConfig(
      name, width, height, enabled, generation, scoring, levels, initLevel, hold, hardDrop, preview, gravity
    )