package extetris.model

enum GameInput:
  case MoveLeft
  case MoveRight
  case MoveDown
  case HardDrop
  case RotateCW
  case RotateCCW
  case Hold
  case Pause
  case Resume
  case Restart
  case Tick
  case ChangeConfig(config: extetris.config.VariantConfig)

object GameInput:
  val movementInputs: Set[GameInput] = Set(MoveLeft, MoveRight, MoveDown, HardDrop, RotateCW, RotateCCW, Hold)
  val systemInputs: Set[GameInput] = Set(Pause, Resume, Restart, Tick)