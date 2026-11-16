package extetris.persistence

import extetris.model.GameState
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.io.{FileWriter, BufferedWriter}

object GamePersistence:
  def save(state: GameState, path: String): Either[Exception, Unit] =
    try
      val json = GameSerializer.serialize(state)
      val filePath = Paths.get(path)
      Files.createDirectories(filePath.getParent)
      Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
      Right(())
    catch
      case e: Exception => Left(e)
  
  def load(path: String): Either[Exception, GameState] =
    try
      val filePath = Paths.get(path)
      if !Files.exists(filePath) then return Left(new java.io.FileNotFoundException(s"Save file not found: $path"))
      val json = Files.readString(filePath)
      GameSerializer.deserialize(json).left.map(e => new Exception(s"Failed to deserialize game: ${e.getMessage}", e))
    catch
      case e: Exception => Left(e)
  
  def saveToFile(state: GameState, file: java.io.File): Either[Exception, Unit] =
    try
      val json = GameSerializer.serialize(state)
      val writer = new BufferedWriter(new FileWriter(file))
      try writer.write(json) finally writer.close()
      Right(())
    catch
      case e: Exception => Left(e)
  
  def loadFromFile(file: java.io.File): Either[Exception, GameState] =
    try
      if !file.exists() then return Left(new java.io.FileNotFoundException(s"Save file not found: ${file.getAbsolutePath}"))
      val source = scala.io.Source.fromFile(file)
      val json = try source.mkString finally source.close()
      GameSerializer.deserialize(json).left.map(e => new Exception(s"Failed to deserialize game: ${e.getMessage}", e))
    catch
      case e: Exception => Left(e)