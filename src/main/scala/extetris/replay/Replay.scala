package extetris.replay

import extetris.model.{GameState, GameInput, GameEvent, GameStatus, Position}
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig, GenerationStrategy}
import extetris.pieces.{PieceId, PieceDefinition, PieceColor, PieceRotation}
import extetris.generation.RandomState
import extetris.board.Board
import extetris.model.Cell
import io.circe.{Encoder, Decoder, Json}
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import io.circe.parser.*

// Re-export codecs from GameSerializer for Replay serialization
import extetris.persistence.GameSerializer.given

final case class Replay(
  version: Int = 1,
  config: VariantConfig,
  initialRandomState: RandomState,
  inputs: List[GameInput],
  events: List[GameEvent] = List.empty
):
  def addInput(input: GameInput): Replay = copy(inputs = inputs :+ input)
  def addEvent(event: GameEvent): Replay = copy(events = events :+ event)
  def withEvents(newEvents: List[GameEvent]): Replay = copy(events = newEvents)

object Replay:
  def record(initialState: GameState, inputs: List[GameInput]): Replay =
    Replay(
      config = initialState.config,
      initialRandomState = initialState.randomState,
      inputs = inputs
    )
  
  def empty(config: VariantConfig, seed: Long): Replay =
    Replay(config = config, initialRandomState = extetris.generation.RandomState.fromSeed(seed), inputs = List.empty)

  given Encoder[Replay] = deriveEncoder[Replay]
  given Decoder[Replay] = deriveDecoder[Replay]
  
  def serialize(replay: Replay): String = replay.asJson.noSpaces
  def deserialize(json: String): Either[io.circe.Error, Replay] = decode[Replay](json)
  
  def save(replay: Replay, path: String): Either[Exception, Unit] =
    try
      val json = serialize(replay)
      java.nio.file.Files.writeString(java.nio.file.Paths.get(path), json)
      Right(())
    catch
      case e: Exception => Left(e)
  
  def load(path: String): Either[Exception, Replay] =
    try
      val json = java.nio.file.Files.readString(java.nio.file.Paths.get(path))
      deserialize(json).left.map(e => new Exception(s"Failed to deserialize replay: ${e.getMessage}", e))
    catch
      case e: Exception => Left(e)