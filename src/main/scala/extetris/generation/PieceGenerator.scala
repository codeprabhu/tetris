package extetris.generation

import extetris.pieces.{PieceId, PieceDefinition, PieceRegistry}
import extetris.config.{PieceGenerationConfig, GenerationStrategy}
import extetris.generation.RandomState

trait PieceGenerator:
  def nextPiece(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState): (Option[PieceId], RandomState)
  def previewPieces(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState, count: Int): (List[PieceId], RandomState)

object PieceGenerator:
  def forStrategy(strategy: GenerationStrategy): PieceGenerator = strategy match
    case GenerationStrategy.Uniform => UniformGenerator
    case GenerationStrategy.Weighted => WeightedGenerator
    case GenerationStrategy.Bag(size) => BagGenerator(size)
    case GenerationStrategy.Custom(className) =>
      try
        val clazz = Class.forName(className)
        clazz.getDeclaredConstructor().newInstance().asInstanceOf[PieceGenerator]
      catch
        case _: Exception => WeightedGenerator

object UniformGenerator extends PieceGenerator:
  def nextPiece(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState): (Option[PieceId], RandomState) =
    val enabled = registry.enabledIds.toVector
    if enabled.isEmpty then (None, randomState)
    else
      val (idx, newState) = randomState.nextInt(enabled.length)
      (Some(enabled(idx)), newState)
  
  def previewPieces(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState, count: Int): (List[PieceId], RandomState) =
    val enabled = registry.enabledIds.toVector
    if enabled.isEmpty then (List.empty, randomState)
    else
      (1 to count).foldLeft((List.empty[PieceId], randomState)) { (acc, _) =>
        val (list, state) = acc
        val (idx, newState) = state.nextInt(enabled.length)
        (list :+ enabled(idx), newState)
      }

object WeightedGenerator extends PieceGenerator:
  def nextPiece(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState): (Option[PieceId], RandomState) =
    val enabled = registry.enabledIds.toVector
    if enabled.isEmpty then return (None, randomState)
    
    val weights = enabled.map(id => config.weightFor(id))
    val totalWeight = weights.sum
    if totalWeight <= 0 then return (None, randomState)
    
    val (rand, newState) = randomState.nextInt(totalWeight)
    var sum = 0
    var selected = enabled.head
    var found = false
    for ((id, w) <- enabled.zip(weights) if !found) do
      sum += w
      if rand < sum then
        selected = id
        found = true
    (Some(selected), newState)
  
  def previewPieces(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState, count: Int): (List[PieceId], RandomState) =
    (1 to count).foldLeft((List.empty[PieceId], randomState)) { (acc, _) =>
      val (list, state) = acc
      val (piece, newState) = nextPiece(registry, config, state)
      piece match
        case Some(p) => (list :+ p, newState)
        case None => (list, newState)
    }

final class BagGenerator(bagSize: Int) extends PieceGenerator:
  private case class BagState(bag: Vector[PieceId], index: Int)
  
  def nextPiece(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState): (Option[PieceId], RandomState) =
    val enabled = registry.enabledIds.toVector
    if enabled.isEmpty then return (None, randomState)
    
    val weights = enabled.map(id => config.weightFor(id))
    val totalWeight = weights.sum
    if totalWeight <= 0 then return (None, randomState)
    
    // For bag, we generate a bag of pieces based on weights
    // This is a simplified implementation - in practice you might want a proper bag algorithm
    val (piece, newState) = WeightedGenerator.nextPiece(registry, config, randomState)
    (piece, newState)
  
  def previewPieces(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState, count: Int): (List[PieceId], RandomState) =
    WeightedGenerator.previewPieces(registry, config, randomState, count)