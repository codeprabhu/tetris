package extetris.config

import extetris.pieces.PieceId

enum GenerationStrategy:
  case Uniform
  case Weighted
  case Bag(size: Int = 7)
  case Custom(className: String)

final case class PieceGenerationConfig(
  strategy: GenerationStrategy = GenerationStrategy.Weighted,
  weights: Map[PieceId, Int] = Map.empty,
  seed: Option[Long] = None,
  bagSize: Int = 7
):
  def weightFor(id: PieceId): Int = weights.getOrElse(id, 1)
  def hasWeights: Boolean = weights.nonEmpty
  
  def withWeight(id: PieceId, weight: Int): PieceGenerationConfig =
    require(weight >= 0, "Weight must be non-negative")
    copy(weights = weights + (id -> weight))
  
  def withoutWeight(id: PieceId): PieceGenerationConfig =
    copy(weights = weights - id)
  
  def withStrategy(strategy: GenerationStrategy): PieceGenerationConfig =
    copy(strategy = strategy)
  
  def withSeed(seed: Long): PieceGenerationConfig =
    copy(seed = Some(seed))

object PieceGenerationConfig:
  val default: PieceGenerationConfig = PieceGenerationConfig()
  
  def uniform(seed: Option[Long] = None): PieceGenerationConfig =
    PieceGenerationConfig(strategy = GenerationStrategy.Uniform, seed = seed)
  
  def weighted(weights: Map[PieceId, Int], seed: Option[Long] = None): PieceGenerationConfig =
    PieceGenerationConfig(strategy = GenerationStrategy.Weighted, weights = weights, seed = seed)
  
  def bag(size: Int = 7, seed: Option[Long] = None): PieceGenerationConfig =
    PieceGenerationConfig(strategy = GenerationStrategy.Bag(size), seed = seed)