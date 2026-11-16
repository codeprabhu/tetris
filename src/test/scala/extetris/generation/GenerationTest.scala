package extetris.generation

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import extetris.pieces.{PieceId, PieceRegistry, StandardPieces}
import extetris.config.{PieceGenerationConfig, GenerationStrategy}

class GenerationTest extends AnyFunSuite with Matchers:
  
  test("Uniform generator produces all pieces"):
    val registry = PieceRegistry.default
    val config = PieceGenerationConfig.uniform(Some(12345L))
    val generator = extetris.generation.UniformGenerator
    
    val pieces = (1 to 1000).foldLeft((Set.empty[PieceId], config.seed.map(extetris.generation.RandomState.fromSeed).getOrElse(extetris.generation.RandomState.random()))) { (acc, _) =>
      val (set, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      (set + piece.get, newState)
    }._1
    
    pieces should contain allElementsOf PieceId.standardIds
  
  test("Weighted generator respects weights"):
    val registry = PieceRegistry.fromDefinitions(Seq(StandardPieces.I, StandardPieces.O))
    val config = PieceGenerationConfig.weighted(Map(PieceId.I -> 100, PieceId.O -> 1), Some(12345L))
    val generator = extetris.generation.WeightedGenerator
    
    val counts = (1 to 10000).foldLeft((Map.empty[PieceId, Int], config.seed.map(extetris.generation.RandomState.fromSeed).getOrElse(extetris.generation.RandomState.random()))) { (acc, _) =>
      val (map, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      piece match
        case Some(p) => (map.updated(p, map.getOrElse(p, 0) + 1), newState)
        case None => (map, newState)
    }._1
    
    counts.getOrElse(PieceId.I, 0) should be > counts.getOrElse(PieceId.O, 0) * 10 // Roughly 100:1 ratio
  
  test("Disabled pieces never generated"):
    val registry = PieceRegistry.default.disable(PieceId.I)
    val config = PieceGenerationConfig.uniform(Some(12345L))
    val generator = extetris.generation.UniformGenerator
    
    val pieces = (1 to 100).foldLeft((Set.empty[PieceId], config.seed.map(extetris.generation.RandomState.fromSeed).getOrElse(extetris.generation.RandomState.random()))) { (acc, _) =>
      val (set, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      (set + piece.get, newState)
    }._1
    
    pieces should not contain PieceId.I
  
  test("Zero weight pieces never generated"):
    val registry = PieceRegistry.default
    val config = PieceGenerationConfig.weighted(Map(PieceId.I -> 0, PieceId.O -> 1), Some(12345L))
    val generator = extetris.generation.WeightedGenerator
    
    val pieces = (1 to 100).foldLeft((Set.empty[PieceId], config.seed.map(extetris.generation.RandomState.fromSeed).getOrElse(extetris.generation.RandomState.random()))) { (acc, _) =>
      val (set, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      (set + piece.get, newState)
    }._1
    
    pieces should not contain PieceId.I
  
  test("Empty registry returns None"):
    val registry = PieceRegistry.empty
    val config = PieceGenerationConfig.uniform(Some(12345L))
    val generator = extetris.generation.UniformGenerator
    
    val (piece, _) = generator.nextPiece(registry, config, extetris.generation.RandomState.fromSeed(12345L))
    piece shouldBe None
  
  test("Deterministic generation with same seed"):
    val registry = PieceRegistry.default
    val config = PieceGenerationConfig.uniform(Some(12345L))
    val generator = extetris.generation.UniformGenerator
    
    val (pieces1, _) = (1 to 10).foldLeft((List.empty[PieceId], extetris.generation.RandomState.fromSeed(12345L))) { (acc, _) =>
      val (list, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      (list :+ piece.get, newState)
    }
    
    val (pieces2, _) = (1 to 10).foldLeft((List.empty[PieceId], extetris.generation.RandomState.fromSeed(12345L))) { (acc, _) =>
      val (list, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      (list :+ piece.get, newState)
    }
    
    pieces1 shouldBe pieces2
  
  test("Different seeds produce different sequences"):
    val registry = PieceRegistry.default
    val config = PieceGenerationConfig.uniform()
    val generator = extetris.generation.UniformGenerator
    
    val (pieces1, _) = (1 to 10).foldLeft((List.empty[PieceId], extetris.generation.RandomState.fromSeed(11111L))) { (acc, _) =>
      val (list, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      (list :+ piece.get, newState)
    }
    
    val (pieces2, _) = (1 to 10).foldLeft((List.empty[PieceId], extetris.generation.RandomState.fromSeed(22222L))) { (acc, _) =>
      val (list, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      (list :+ piece.get, newState)
    }
    
    pieces1 should not equal pieces2
  
  test("Preview pieces"):
    val registry = PieceRegistry.default
    val config = PieceGenerationConfig.uniform(Some(12345L))
    val generator = extetris.generation.UniformGenerator
    
    val (preview, _) = generator.previewPieces(registry, config, extetris.generation.RandomState.fromSeed(12345L), 3)
    preview.size shouldBe 3
    preview.foreach(p => registry.enabledIds should contain(p))
  
  test("Bag generator"):
    val registry = PieceRegistry.default
    val config = PieceGenerationConfig.bag(7, Some(12345L))
    val generator = extetris.generation.BagGenerator(7)
    
    val (pieces, _) = (1 to 20).foldLeft((List.empty[PieceId], config.seed.map(extetris.generation.RandomState.fromSeed).getOrElse(extetris.generation.RandomState.random()))) { (acc, _) =>
      val (list, state) = acc
      val (piece, newState) = generator.nextPiece(registry, config, state)
      (list :+ piece.get, newState)
    }
    
    pieces.size shouldBe 20
    pieces.foreach(p => registry.enabledIds should contain(p))