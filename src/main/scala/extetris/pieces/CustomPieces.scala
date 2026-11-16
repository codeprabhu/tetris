package extetris.pieces

import extetris.model.Position

object CustomPieces:
  val Single: PieceDefinition = PieceDefinition.custom(
    PieceId.Single, "Single", PieceColor.forPieceId(PieceId.Single),
    Set(Position(0, 0)),
    RotationBehavior.None
  )
  
  val L3: PieceDefinition = PieceDefinition.custom(
    PieceId.L3, "L3", PieceColor.forPieceId(PieceId.L3),
    Set(Position(0, 0), Position(0, 1), Position(1, 1))
  )
  
  val Plus: PieceDefinition = PieceDefinition.custom(
    PieceId.Plus, "Plus", PieceColor.forPieceId(PieceId.Plus),
    Set(Position(1, 0), Position(0, 1), Position(1, 1), Position(2, 1), Position(1, 2))
  )
  
  val all: Map[PieceId, PieceDefinition] = Map(
    PieceId.Single -> Single,
    PieceId.L3 -> L3,
    PieceId.Plus -> Plus
  )