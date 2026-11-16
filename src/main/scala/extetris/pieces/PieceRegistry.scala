package extetris.pieces

import extetris.config.ValidationError

final case class PieceRegistry private (
  definitions: Map[PieceId, PieceDefinition],
  enabled: Set[PieceId]
):
  def get(id: PieceId): Option[PieceDefinition] = definitions.get(id)
  def getEnabled(id: PieceId): Option[PieceDefinition] = 
    if enabled.contains(id) then definitions.get(id) else None
  
  def enabledDefinitions: Map[PieceId, PieceDefinition] = 
    definitions.filter((id, _) => enabled.contains(id))
  
  def enabledIds: Set[PieceId] = enabled
  def allIds: Set[PieceId] = definitions.keySet
  
  def enable(id: PieceId): Either[ValidationError, PieceRegistry] =
    if definitions.contains(id) then Right(copy(enabled = enabled + id))
    else Left(ValidationError(s"Piece '$id' is not defined in the registry"))
  
  def disable(id: PieceId): PieceRegistry =
    copy(enabled = enabled - id)
  
  def withDefinition(definition: PieceDefinition): PieceRegistry =
    copy(definitions = definitions + (definition.id -> definition), enabled = enabled + definition.id)
  
  def withoutDefinition(id: PieceId): PieceRegistry =
    copy(definitions = definitions - id, enabled = enabled - id)

object PieceRegistry:
  val default: PieceRegistry = PieceRegistry(
    definitions = StandardPieces.all ++ CustomPieces.all,
    enabled = StandardPieces.all.keySet
  )
  
  def empty: PieceRegistry = PieceRegistry(Map.empty, Set.empty)
  
  def fromDefinitions(defs: Map[PieceId, PieceDefinition], enabledIds: Set[PieceId]): PieceRegistry =
    PieceRegistry(defs, enabledIds.intersect(defs.keySet))
  
  def fromDefinitions(defs: Seq[PieceDefinition]): PieceRegistry =
    val map = defs.map(d => d.id -> d).toMap
    PieceRegistry(map, map.keySet)