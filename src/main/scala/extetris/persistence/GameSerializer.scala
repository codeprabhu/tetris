package extetris.persistence

import extetris.model.{GameState, GameInput, GameEvent, GameStatus, Position}
import extetris.board.Board
import extetris.pieces.{Piece, PieceDefinition, PieceId, PieceColor, PieceRotation}
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig, GenerationStrategy}
import extetris.generation.RandomState
import io.circe.{Encoder, Decoder, Json, HCursor, JsonObject}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.parser.*

object GameSerializer:
  // Position codecs
  given Encoder[Position] = Encoder.instance { pos =>
    Json.obj("x" -> pos.x.asJson, "y" -> pos.y.asJson)
  }
  given Decoder[Position] = Decoder.instance { c =>
    for
      x <- c.downField("x").as[Int]
      y <- c.downField("y").as[Int]
    yield Position(x, y)
  }
  
  // PieceId codecs
  given Encoder[PieceId] = Encoder.encodeString.contramap(_.value)
  given Decoder[PieceId] = Decoder.decodeString.map(PieceId.apply)
  given io.circe.KeyEncoder[PieceId] = io.circe.KeyEncoder.encodeKeyString.contramap(_.value)
  given io.circe.KeyDecoder[PieceId] = io.circe.KeyDecoder.decodeKeyString.map(PieceId.apply)
  
  // PieceColor codecs
  given Encoder[PieceColor] = Encoder.instance { c =>
    Json.obj("r" -> c.r.asJson, "g" -> c.g.asJson, "b" -> c.b.asJson, "a" -> c.a.asJson)
  }
  given Decoder[PieceColor] = Decoder.instance { c =>
    for
      r <- c.downField("r").as[Double]
      g <- c.downField("g").as[Double]
      b <- c.downField("b").as[Double]
      a <- c.downField("a").as[Option[Double]].map(_.getOrElse(1.0))
    yield PieceColor(r, g, b, a)
  }
  
  // RotationBehavior codecs
  given Encoder[extetris.pieces.RotationBehavior] = Encoder.instance {
    case extetris.pieces.RotationBehavior.Full => Json.obj("type" -> "Full".asJson)
    case extetris.pieces.RotationBehavior.Limited(states) => Json.obj("type" -> "Limited".asJson, "states" -> states.asJson)
    case extetris.pieces.RotationBehavior.None => Json.obj("type" -> "None".asJson)
  }
  given Decoder[extetris.pieces.RotationBehavior] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "Full" => Right(extetris.pieces.RotationBehavior.Full)
      case "Limited" => c.downField("states").as[Int].map(extetris.pieces.RotationBehavior.Limited.apply)
      case "None" => Right(extetris.pieces.RotationBehavior.None)
      case other => Left(io.circe.DecodingFailure(s"Unknown rotation behavior: $other", c.history))
    }
  }
  
  // PieceRotation codecs
  given Encoder[PieceRotation] = Encoder.instance { rot =>
    Json.obj(
      "cells" -> rot.cells.toVector.asJson,
      "origin" -> rot.origin.asJson
    )
  }
  given Decoder[PieceRotation] = Decoder.instance { c =>
    for
      cells <- c.downField("cells").as[Vector[Position]].map(_.toSet)
      origin <- c.downField("origin").as[Position]
    yield PieceRotation(cells, origin)
  }
  
  // PieceDefinition codecs
  given Encoder[PieceDefinition] = Encoder.instance { defn =>
    Json.obj(
      "id" -> defn.id.asJson,
      "name" -> defn.name.asJson,
      "color" -> defn.color.asJson,
      "rotations" -> defn.rotations.asJson,
      "behavior" -> defn.behavior.asJson
    )
  }
  given Decoder[PieceDefinition] = Decoder.instance { c =>
    for
      id <- c.downField("id").as[PieceId]
      name <- c.downField("name").as[String]
      color <- c.downField("color").as[PieceColor]
      rotations <- c.downField("rotations").as[Vector[PieceRotation]]
      behavior <- c.downField("behavior").as[extetris.pieces.RotationBehavior]
    yield extetris.pieces.PieceDefinition.custom(id, name, color, rotations.head.cells, behavior)
  }
  
  // Cell codecs
  given Encoder[extetris.model.Cell] = Encoder.instance {
    case extetris.model.Cell.Empty => Json.obj("type" -> "Empty".asJson)
    case extetris.model.Cell.Filled(pieceId, color) =>
      Json.obj("type" -> "Filled".asJson, "pieceId" -> pieceId.asJson, "color" -> color.asJson)
  }
  given Decoder[extetris.model.Cell] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "Empty" => Right(extetris.model.Cell.Empty)
      case "Filled" =>
        for
          pieceId <- c.downField("pieceId").as[PieceId]
          color <- c.downField("color").as[PieceColor]
        yield extetris.model.Cell.Filled(pieceId, color)
      case other => Left(io.circe.DecodingFailure(s"Unknown cell type: $other", c.history))
    }
  }
  
  // Board codecs
  given Encoder[Board] = Encoder.instance { board =>
    Json.obj(
      "width" -> board.width.asJson,
      "height" -> board.height.asJson,
      "grid" -> board.grid.asJson
    )
  }
  given Decoder[Board] = Decoder.instance { c =>
    for
      width <- c.downField("width").as[Int]
      height <- c.downField("height").as[Int]
      grid <- c.downField("grid").as[Vector[Vector[extetris.model.Cell]]]
    yield Board.fromGrid(grid)
  }
  
  // Piece codecs
  given Encoder[Piece] = Encoder.instance { piece =>
    Json.obj(
      "definition" -> piece.definition.asJson,
      "rotation" -> piece.rotation.asJson,
      "position" -> piece.position.asJson
    )
  }
  given Decoder[Piece] = Decoder.instance { c =>
    for
      definition <- c.downField("definition").as[PieceDefinition]
      rotation <- c.downField("rotation").as[PieceRotation]
      position <- c.downField("position").as[Position]
    yield Piece(definition, rotation, position)
  }
  
  // GameStatus codecs
  given Encoder[GameStatus] = Encoder.encodeString.contramap(_.toString)
  given Decoder[GameStatus] = Decoder.decodeString.emap {
    case "Playing" => Right(GameStatus.Playing)
    case "Paused" => Right(GameStatus.Paused)
    case "GameOver" => Right(GameStatus.GameOver)
    case "NotStarted" => Right(GameStatus.NotStarted)
    case other => Left(s"Unknown game status: $other")
  }
  
  // GameInput codecs
  given Encoder[GameInput] = Encoder.instance {
    case GameInput.MoveLeft => Json.obj("type" -> "MoveLeft".asJson)
    case GameInput.MoveRight => Json.obj("type" -> "MoveRight".asJson)
    case GameInput.MoveDown => Json.obj("type" -> "MoveDown".asJson)
    case GameInput.HardDrop => Json.obj("type" -> "HardDrop".asJson)
    case GameInput.RotateCW => Json.obj("type" -> "RotateCW".asJson)
    case GameInput.RotateCCW => Json.obj("type" -> "RotateCCW".asJson)
    case GameInput.Hold => Json.obj("type" -> "Hold".asJson)
    case GameInput.Pause => Json.obj("type" -> "Pause".asJson)
    case GameInput.Resume => Json.obj("type" -> "Resume".asJson)
    case GameInput.Restart => Json.obj("type" -> "Restart".asJson)
    case GameInput.Tick => Json.obj("type" -> "Tick".asJson)
    case GameInput.ChangeConfig(config) => Json.obj("type" -> "ChangeConfig".asJson, "config" -> config.asJson)
  }
  given Decoder[GameInput] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "MoveLeft" => Right(GameInput.MoveLeft)
      case "MoveRight" => Right(GameInput.MoveRight)
      case "MoveDown" => Right(GameInput.MoveDown)
      case "HardDrop" => Right(GameInput.HardDrop)
      case "RotateCW" => Right(GameInput.RotateCW)
      case "RotateCCW" => Right(GameInput.RotateCCW)
      case "Hold" => Right(GameInput.Hold)
      case "Pause" => Right(GameInput.Pause)
      case "Resume" => Right(GameInput.Resume)
      case "Restart" => Right(GameInput.Restart)
      case "Tick" => Right(GameInput.Tick)
      case "ChangeConfig" => c.downField("config").as[VariantConfig].map(GameInput.ChangeConfig.apply)
      case other => Left(io.circe.DecodingFailure(s"Unknown input type: $other", c.history))
    }
  }
  
  // GameEvent codecs
  given Encoder[GameEvent] = Encoder.instance {
    case GameEvent.PieceSpawned(piece) => Json.obj("type" -> "PieceSpawned".asJson, "piece" -> piece.asJson)
    case GameEvent.PieceMoved(piece, from, to) => Json.obj("type" -> "PieceMoved".asJson, "piece" -> piece.asJson, "from" -> from.asJson, "to" -> to.asJson)
    case GameEvent.PieceRotated(piece, from, to) => Json.obj("type" -> "PieceRotated".asJson, "piece" -> piece.asJson, "from" -> from.asJson, "to" -> to.asJson)
    case GameEvent.PieceHardDropped(piece, from, to) => Json.obj("type" -> "PieceHardDropped".asJson, "piece" -> piece.asJson, "from" -> from.asJson, "to" -> to.asJson)
    case GameEvent.PieceLocked(piece) => Json.obj("type" -> "PieceLocked".asJson, "piece" -> piece.asJson)
    case GameEvent.LinesCleared(count, board) => Json.obj("type" -> "LinesCleared".asJson, "count" -> count.asJson, "board" -> board.asJson)
    case GameEvent.ScoreChanged(oldScore, newScore) => Json.obj("type" -> "ScoreChanged".asJson, "oldScore" -> oldScore.asJson, "newScore" -> newScore.asJson)
    case GameEvent.LevelChanged(oldLevel, newLevel) => Json.obj("type" -> "LevelChanged".asJson, "oldLevel" -> oldLevel.asJson, "newLevel" -> newLevel.asJson)
    case GameEvent.GameOver(finalScore, finalLevel, linesCleared) => Json.obj("type" -> "GameOver".asJson, "finalScore" -> finalScore.asJson, "finalLevel" -> finalLevel.asJson, "linesCleared" -> linesCleared.asJson)
    case GameEvent.GamePaused => Json.obj("type" -> "GamePaused".asJson)
    case GameEvent.GameResumed => Json.obj("type" -> "GameResumed".asJson)
    case GameEvent.GameRestarted => Json.obj("type" -> "GameRestarted".asJson)
    case GameEvent.ConfigChanged(config) => Json.obj("type" -> "ConfigChanged".asJson, "config" -> config.asJson)
    case GameEvent.HoldUsed(held, current) => Json.obj("type" -> "HoldUsed".asJson, "held" -> held.asJson, "current" -> current.asJson)
  }
  given Decoder[GameEvent] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "PieceSpawned" => c.downField("piece").as[Piece].map(GameEvent.PieceSpawned.apply)
      case "PieceMoved" => for { piece <- c.downField("piece").as[Piece]; from <- c.downField("from").as[Position]; to <- c.downField("to").as[Position] } yield GameEvent.PieceMoved(piece, from, to)
      case "PieceRotated" => for { piece <- c.downField("piece").as[Piece]; from <- c.downField("from").as[PieceRotation]; to <- c.downField("to").as[PieceRotation] } yield GameEvent.PieceRotated(piece, from, to)
      case "PieceHardDropped" => for { piece <- c.downField("piece").as[Piece]; from <- c.downField("from").as[Position]; to <- c.downField("to").as[Position] } yield GameEvent.PieceHardDropped(piece, from, to)
      case "PieceLocked" => c.downField("piece").as[Piece].map(GameEvent.PieceLocked.apply)
      case "LinesCleared" => for { count <- c.downField("count").as[Int]; board <- c.downField("board").as[Board] } yield GameEvent.LinesCleared(count, board)
      case "ScoreChanged" => for { old <- c.downField("oldScore").as[Long]; newS <- c.downField("newScore").as[Long] } yield GameEvent.ScoreChanged(old, newS)
      case "LevelChanged" => for { old <- c.downField("oldLevel").as[Int]; newL <- c.downField("newLevel").as[Int] } yield GameEvent.LevelChanged(old, newL)
      case "GameOver" => for { score <- c.downField("finalScore").as[Long]; level <- c.downField("finalLevel").as[Int]; lines <- c.downField("linesCleared").as[Int] } yield GameEvent.GameOver(score, level, lines)
      case "GamePaused" => Right(GameEvent.GamePaused)
      case "GameResumed" => Right(GameEvent.GameResumed)
      case "GameRestarted" => Right(GameEvent.GameRestarted)
      case "ConfigChanged" => c.downField("config").as[VariantConfig].map(GameEvent.ConfigChanged.apply)
      case "HoldUsed" => for { held <- c.downField("held").as[Option[PieceDefinition]]; current <- c.downField("current").as[Piece] } yield GameEvent.HoldUsed(held, current)
      case other => Left(io.circe.DecodingFailure(s"Unknown event type: $other", c.history))
    }
  }
  
  // RandomState codecs
  given Encoder[RandomState] = Encoder.instance { rs =>
    Json.obj("seed" -> rs.currentSeed.asJson, "state" -> rs.currentState.asJson)
  }
  given Decoder[RandomState] = Decoder.instance { c =>
    for
      seed <- c.downField("seed").as[Long]
      state <- c.downField("state").as[Long]
    yield RandomState(seed, state)
  }
  
  // VariantConfig codecs
  given Encoder[VariantConfig] = deriveEncoder[VariantConfig]
  given Decoder[VariantConfig] = deriveDecoder[VariantConfig]
  
  // ScoringConfig codecs
  given Encoder[ScoringConfig] = deriveEncoder[ScoringConfig]
  given Decoder[ScoringConfig] = deriveDecoder[ScoringConfig]
  
  // LevelConfig codecs
  given Encoder[LevelConfig] = deriveEncoder[LevelConfig]
  given Decoder[LevelConfig] = deriveDecoder[LevelConfig]
  
  // GenerationStrategy codecs
  given Encoder[GenerationStrategy] = Encoder.instance {
    case GenerationStrategy.Uniform => Json.obj("type" -> "Uniform".asJson)
    case GenerationStrategy.Weighted => Json.obj("type" -> "Weighted".asJson)
    case GenerationStrategy.Bag(size) => Json.obj("type" -> "Bag".asJson, "size" -> size.asJson)
    case GenerationStrategy.Custom(className) => Json.obj("type" -> "Custom".asJson, "className" -> className.asJson)
  }
  given Decoder[GenerationStrategy] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "Uniform" => Right(GenerationStrategy.Uniform)
      case "Weighted" => Right(GenerationStrategy.Weighted)
      case "Bag" => c.downField("size").as[Int].map(GenerationStrategy.Bag.apply)
      case "Custom" => c.downField("className").as[String].map(GenerationStrategy.Custom.apply)
      case other => Left(io.circe.DecodingFailure(s"Unknown generation strategy: $other", c.history))
    }
  }
  
  // PieceGenerationConfig codecs
  given Encoder[PieceGenerationConfig] = Encoder.instance { cfg =>
    Json.obj(
      "strategy" -> cfg.strategy.asJson,
      "weights" -> cfg.weights.asJson,
      "seed" -> cfg.seed.asJson,
      "bagSize" -> cfg.bagSize.asJson
    )
  }
  given Decoder[PieceGenerationConfig] = Decoder.instance { c =>
    for
      strategy <- c.downField("strategy").as[GenerationStrategy]
      weights <- c.downField("weights").as[Map[PieceId, Int]]
      seed <- c.downField("seed").as[Option[Long]]
      bagSize <- c.downField("bagSize").as[Int]
    yield PieceGenerationConfig(strategy, weights, seed, bagSize)
  }
  
  // GameState codecs
  given Encoder[GameState] = Encoder.instance { state =>
    Json.obj(
      "board" -> state.board.asJson,
      "currentPiece" -> state.currentPiece.asJson,
      "nextPiece" -> state.nextPiece.asJson,
      "heldPiece" -> state.heldPiece.asJson,
      "canHold" -> state.canHold.asJson,
      "score" -> state.score.asJson,
      "level" -> state.level.asJson,
      "linesCleared" -> state.linesCleared.asJson,
      "status" -> state.status.asJson,
      "config" -> state.config.asJson,
      "randomState" -> state.randomState.asJson,
      "dropCounter" -> state.dropCounter.asJson,
      "dropInterval" -> state.dropInterval.asJson,
      "lastAction" -> state.lastAction.asJson
    )
  }
  given Decoder[GameState] = Decoder.instance { c =>
    for
      board <- c.downField("board").as[Board]
      currentPiece <- c.downField("currentPiece").as[Option[Piece]]
      nextPiece <- c.downField("nextPiece").as[Option[Piece]]
      heldPiece <- c.downField("heldPiece").as[Option[PieceDefinition]]
      canHold <- c.downField("canHold").as[Boolean]
      score <- c.downField("score").as[Long]
      level <- c.downField("level").as[Int]
      linesCleared <- c.downField("linesCleared").as[Int]
      status <- c.downField("status").as[GameStatus]
      config <- c.downField("config").as[VariantConfig]
      randomState <- c.downField("randomState").as[RandomState]
      dropCounter <- c.downField("dropCounter").as[Int]
      dropInterval <- c.downField("dropInterval").as[Int]
      lastAction <- c.downField("lastAction").as[Option[GameInput]]
    yield GameState(board, currentPiece, nextPiece, heldPiece, canHold, score, level, linesCleared, status, config, randomState, dropCounter, dropInterval, lastAction)
  }
  
  def serialize(state: GameState): String = state.asJson.noSpaces
  def deserialize(json: String): Either[io.circe.Error, GameState] = decode[GameState](json)