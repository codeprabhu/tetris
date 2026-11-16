package extetris.pieces

import extetris.model.Position

object StandardPieces:
  val I: PieceDefinition = PieceDefinition.standard(
    PieceId.I, "I", PieceColor.forPieceId(PieceId.I),
    Set(Position(0, 1), Position(1, 1), Position(2, 1), Position(3, 1)),
    RotationBehavior.Limited(2)
  )
  
  val O: PieceDefinition = PieceDefinition.standard(
    PieceId.O, "O", PieceColor.forPieceId(PieceId.O),
    Set(Position(0, 0), Position(1, 0), Position(0, 1), Position(1, 1)),
    RotationBehavior.None
  )
  
  val T: PieceDefinition = PieceDefinition.standard(
    PieceId.T, "T", PieceColor.forPieceId(PieceId.T),
    Set(Position(0, 1), Position(1, 1), Position(2, 1), Position(1, 0))
  )
  
  val S: PieceDefinition = PieceDefinition.standard(
    PieceId.S, "S", PieceColor.forPieceId(PieceId.S),
    Set(Position(1, 1), Position(2, 1), Position(0, 0), Position(1, 0)),
    RotationBehavior.Limited(2)
  )
  
  val Z: PieceDefinition = PieceDefinition.standard(
    PieceId.Z, "Z", PieceColor.forPieceId(PieceId.Z),
    Set(Position(0, 1), Position(1, 1), Position(1, 0), Position(2, 0)),
    RotationBehavior.Limited(2)
  )
  
  val J: PieceDefinition = PieceDefinition.standard(
    PieceId.J, "J", PieceColor.forPieceId(PieceId.J),
    Set(Position(0, 1), Position(1, 1), Position(2, 1), Position(0, 0))
  )
  
  val L: PieceDefinition = PieceDefinition.standard(
    PieceId.L, "L", PieceColor.forPieceId(PieceId.L),
    Set(Position(0, 1), Position(1, 1), Position(2, 1), Position(2, 0))
  )
  
  val all: Map[PieceId, PieceDefinition] = Map(
    PieceId.I -> I,
    PieceId.O -> O,
    PieceId.T -> T,
    PieceId.S -> S,
    PieceId.Z -> Z,
    PieceId.J -> J,
    PieceId.L -> L
  )