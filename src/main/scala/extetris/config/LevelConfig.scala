package extetris.config

final case class LevelConfig(
  linesPerLevel: Int = 10,
  initialDropInterval: Int = 1000,
  minDropInterval: Int = 50,
  dropIntervalDecrease: Int = 50,
  maxLevel: Int = 20
):
  def dropIntervalForLevel(level: Int): Int =
    val clampedLevel = level.max(1).min(maxLevel)
    val interval = initialDropInterval - (clampedLevel - 1) * dropIntervalDecrease
    interval.max(minDropInterval)
  
  def levelForLines(linesCleared: Int): Int =
    1 + linesCleared / linesPerLevel
  
  def linesForLevel(level: Int): Int =
    (level - 1) * linesPerLevel

object LevelConfig:
  val default: LevelConfig = LevelConfig()
  val fast: LevelConfig = LevelConfig(
    linesPerLevel = 5,
    initialDropInterval = 800,
    minDropInterval = 30,
    dropIntervalDecrease = 60,
    maxLevel = 30
  )