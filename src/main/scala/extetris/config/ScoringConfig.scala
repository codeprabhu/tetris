package extetris.config

final case class ScoringConfig(
  singleLine: Int = 100,
  doubleLine: Int = 300,
  tripleLine: Int = 500,
  tetrisLine: Int = 800,
  softDropPerCell: Int = 1,
  hardDropPerCell: Int = 2,
  levelMultiplier: Boolean = true
):
  def scoreForLines(lines: Int, level: Int): Int =
    val base = lines match
      case 1 => singleLine
      case 2 => doubleLine
      case 3 => tripleLine
      case 4 => tetrisLine
      case _ => 0
    if levelMultiplier then base * level else base
  
  def softDropScore(cells: Int): Int = softDropPerCell * cells
  def hardDropScore(cells: Int): Int = hardDropPerCell * cells

object ScoringConfig:
  val default: ScoringConfig = ScoringConfig()
  val classic: ScoringConfig = ScoringConfig(
    singleLine = 40,
    doubleLine = 100,
    tripleLine = 300,
    tetrisLine = 1200,
    softDropPerCell = 1,
    hardDropPerCell = 2,
    levelMultiplier = true
  )