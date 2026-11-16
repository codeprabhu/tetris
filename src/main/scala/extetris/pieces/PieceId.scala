package extetris.pieces

opaque type PieceId = String

object PieceId:
  def apply(value: String): PieceId = value
  
  val I: PieceId = "I"
  val O: PieceId = "O"
  val T: PieceId = "T"
  val S: PieceId = "S"
  val Z: PieceId = "Z"
  val J: PieceId = "J"
  val L: PieceId = "L"
  val Single: PieceId = "Single"
  val L3: PieceId = "L3"
  val Plus: PieceId = "Plus"
  
  val standardIds: Set[PieceId] = Set(I, O, T, S, Z, J, L)
  val customIds: Set[PieceId] = Set(Single, L3, Plus)
  val allBuiltInIds: Set[PieceId] = standardIds ++ customIds
  
  extension (id: PieceId)
    def value: String = id
    def isStandard: Boolean = standardIds.contains(id)
    def isCustom: Boolean = customIds.contains(id)
    def isBuiltIn: Boolean = allBuiltInIds.contains(id)