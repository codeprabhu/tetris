# ExTetris Platform API Reference

## Package: `extetris.api`

### `ExTetrisAPI` - Main Entry Point

```scala
object ExTetrisAPI
```

#### Configuration

```scala
def createVariant(config: VariantConfig): Either[ValidationError, VariantConfig]
def defaultVariant: VariantConfig
def classicVariant: VariantConfig
def extendedVariant: VariantConfig
```

#### Piece Definition

```scala
def defineStandardPiece(
  id: PieceId,
  name: String,
  color: PieceColor,
  cells: Set[Position],
  behavior: RotationBehavior = RotationBehavior.Full
): PieceDefinition

def defineCustomPiece(
  id: PieceId,
  name: String,
  color: PieceColor,
  cells: Set[Position],
  behavior: RotationBehavior = RotationBehavior.Full
): PieceDefinition
```

#### Game Control

```scala
def newGame(config: VariantConfig, seed: Option[Long] = None): GameState
def applyInput(state: GameState, input: GameInput): (GameState, List[GameEvent])
def tick(state: GameState): (GameState, List[GameEvent])
```

#### State Inspection

```scala
def getBoard(state: GameState): Board
def getCurrentPiece(state: GameState): Option[Piece]
def getNextPiece(state: GameState): Option[Piece]
def getHeldPiece(state: GameState): Option[PieceDefinition]
def getScore(state: GameState): Long
def getLevel(state: GameState): Int
def getLinesCleared(state: GameState): Int
def getStatus(state: GameState): GameStatus
def getConfig(state: GameState): VariantConfig
def isGameOver(state: GameState): Boolean
def isPlaying(state: GameState): Boolean
```

#### Persistence

```scala
def saveGame(state: GameState, path: String): Either[Exception, Unit]
def loadGame(path: String): Either[Exception, GameState]
def serializeGame(state: GameState): String
def deserializeGame(json: String): Either[Exception, GameState]
```

#### Replay

```scala
def recordReplay(initialState: GameState, inputs: List[GameInput]): Replay
def playReplay(replay: Replay): List[(GameState, GameInput, List[GameEvent], GameState)]
```

#### Configuration Builders

```scala
def scoringConfig(
  single: Int = 100,
  double: Int = 300,
  triple: Int = 500,
  tetris: Int = 800,
  softDrop: Int = 1,
  hardDrop: Int = 2,
  levelMultiplier: Boolean = true
): ScoringConfig

def levelConfig(
  linesPerLevel: Int = 10,
  initialDropInterval: Int = 1000,
  minDropInterval: Int = 50,
  dropIntervalDecrease: Int = 50,
  maxLevel: Int = 20
): LevelConfig

def generationConfig(
  strategy: GenerationStrategy = GenerationStrategy.Weighted,
  weights: Map[PieceId, Int] = Map.empty,
  seed: Option[Long] = None
): PieceGenerationConfig
```

#### Piece Registry

```scala
def defaultRegistry: PieceRegistry
def emptyRegistry: PieceRegistry
def registryFrom(definitions: Seq[PieceDefinition]): PieceRegistry
```

---

## Package: `extetris.model`

### `Position`

```scala
case class Position(x: Int, y: Int)
```

**Operations:**
- `+(other): Position`
- `-(other): Position`
- `*(scalar): Position`
- `translate(dx, dy): Position`
- `rotateCW(origin = Position(0,0)): Position`
- `rotateCCW(origin = Position(0,0)): Position`
- `inBounds(width, height): Boolean`

### `Cell`

```scala
enum Cell:
  case Empty
  case Filled(pieceId: PieceId, color: PieceColor)
```

**Extensions:**
- `isEmpty: Boolean`
- `isFilled: Boolean`
- `pieceId: Option[PieceId]`
- `color: Option[PieceColor]`

### `GameState`

```scala
case class GameState(
  board: Board,
  currentPiece: Option[Piece],
  nextPiece: Option[Piece],
  heldPiece: Option[PieceDefinition],
  canHold: Boolean,
  score: Long,
  level: Int,
  linesCleared: Int,
  status: GameStatus,
  config: VariantConfig,
  randomState: RandomState,
  dropCounter: Int,
  dropInterval: Int,
  lastAction: Option[GameInput]
)
```

**Methods:**
- `isPlaying: Boolean`
- `isPaused: Boolean`
- `isGameOver: Boolean`
- `isNotStarted: Boolean`
- `withStatus(s): GameState`
- `withBoard(b): GameState`
- `withCurrentPiece(p): GameState`
- `withNextPiece(p): GameState`
- `withHeldPiece(d): GameState`
- `resetHold: GameState`
- `addScore(points): GameState`
- `addLinesCleared(count): GameState`
- `setLevel(l): GameState`
- `setDropInterval(i): GameState`
- `incrementDropCounter: GameState`
- `resetDropCounter: GameState`
- `withRandomState(rs): GameState`
- `withLastAction(a): GameState`
- `withConfig(c): GameState`

### `GameStatus`

```scala
enum GameStatus:
  case Playing, Paused, GameOver, NotStarted
```

### `GameInput`

```scala
enum GameInput:
  case MoveLeft, MoveRight, MoveDown
  case HardDrop
  case RotateCW, RotateCCW
  case Hold
  case Pause, Resume, Restart
  case Tick
  case ChangeConfig(config: VariantConfig)
```

### `GameEvent`

```scala
enum GameEvent:
  case PieceSpawned(piece: Piece)
  case PieceMoved(piece: Piece, from: Position, to: Position)
  case PieceRotated(piece: Piece, from: PieceRotation, to: PieceRotation)
  case PieceHardDropped(piece: Piece, from: Position, to: Position)
  case PieceLocked(piece: Piece)
  case LinesCleared(count: Int, board: Board)
  case ScoreChanged(oldScore: Long, newScore: Long)
  case LevelChanged(oldLevel: Int, newLevel: Int)
  case GameOver(finalScore: Long, finalLevel: Int, linesCleared: Int)
  case GamePaused, GameResumed, GameRestarted
  case ConfigChanged(config: VariantConfig)
  case HoldUsed(held: Option[PieceDefinition], current: Piece)
```

---

## Package: `extetris.pieces`

### `PieceId`

```scala
opaque type PieceId = String

object PieceId:
  val I, O, T, S, Z, J, L: PieceId
  val Single, L3, Plus: PieceId
  val standardIds: Set[PieceId]
  val customIds: Set[PieceId]
  val allBuiltInIds: Set[PieceId]
```

### `PieceColor`

```scala
case class PieceColor(r: Double, g: Double, b: Double, a: Double = 1.0):
  def toScalafxColor: Color
  def toHex: String

object PieceColor:
  val StandardColors: Map[PieceId, PieceColor]
  def forPieceId(id: PieceId): PieceColor
```

### `RotationBehavior`

```scala
enum RotationBehavior:
  case Full
  case Limited(states: Int)
  case None
```

### `PieceRotation`

```scala
case class PieceRotation(cells: Set[Position], origin: Position = Position(0,0)):
  def rotateCW: PieceRotation
  def rotateCCW: PieceRotation
  def translate(dx, dy): PieceRotation
  def width: Int
  def height: Int
  def boundingBox: (Int, Int, Int, Int)  // (minX, minY, width, height)
```

### `PieceDefinition`

```scala
sealed trait PieceDefinition:
  def id: PieceId
  def name: String
  def color: PieceColor
  def rotations: Vector[PieceRotation]
  def behavior: RotationBehavior
  def rotationCount: Int
  def initialRotation: PieceRotation
  def nextRotation(current: PieceRotation): PieceRotation
  def prevRotation(current: PieceRotation): PieceRotation
```

**Factory Methods:**
```scala
object PieceDefinition:
  def standard(id, name, color, cells, behavior = Full): PieceDefinition
  def custom(id, name, color, cells, behavior = Full): PieceDefinition
```

### `Piece`

```scala
case class Piece(definition: PieceDefinition, rotation: PieceRotation, position: Position):
  def cells: Set[Position]
  def move(dx, dy): Piece
  def moveTo(pos): Piece
  def rotateCW: Piece
  def rotateCCW: Piece
  def withRotation(rot): Piece
  def id: PieceId
  def color: PieceColor
  def name: String
  def boundingBox: (Int, Int, Int, Int)

object Piece:
  def spawn(definition: PieceDefinition, boardWidth: Int): Piece
```

### `PieceRegistry`

```scala
case class PieceRegistry(definitions: Map[PieceId, PieceDefinition], enabled: Set[PieceId]):
  def get(id): Option[PieceDefinition]
  def getEnabled(id): Option[PieceDefinition]
  def enabledDefinitions: Map[PieceId, PieceDefinition]
  def enabledIds: Set[PieceId]
  def allIds: Set[PieceId]
  def enable(id): Either[ValidationError, PieceRegistry]
  def disable(id): PieceRegistry
  def withDefinition(defn): PieceRegistry
  def withoutDefinition(id): PieceRegistry

object PieceRegistry:
  val default: PieceRegistry
  val empty: PieceRegistry
  def fromDefinitions(defs: Map[PieceId, PieceDefinition], enabledIds: Set[PieceId]): PieceRegistry
  def fromDefinitions(defs: Seq[PieceDefinition]): PieceRegistry
```

---

## Package: `extetris.board`

### `Board`

```scala
case class Board(width: Int, height: Int, grid: Vector[Vector[Cell]]):
  def cellAt(pos: Position): Option[Cell]
  def cellAt(x, y): Option[Cell]
  def isEmpty(pos: Position): Boolean
  def isOccupied(pos: Position): Boolean
  def canPlace(piece: Piece): Boolean
  def place(piece: Piece): Board
  def clearLines: (Board, Int)
  def heights: Vector[Int]
  def aggregateHeight: Int
  def maxHeight: Int
  def holes: Int
  def bumpiness: Int
  def isEmpty: Boolean
  def isFull: Boolean
  def row(y): Option[Vector[Cell]]
  def column(x): Option[Vector[Cell]]
  def toVector: Vector[Vector[Cell]]

object Board:
  def empty(width: Int, height: Int): Board
  def fromGrid(grid: Vector[Vector[Cell]]): Board
```

---

## Package: `extetris.config`

### `VariantConfig`

```scala
case class VariantConfig(
  name: String,
  boardWidth: Int = 10,
  boardHeight: Int = 20,
  enabledPieces: Set[PieceId] = PieceId.standardIds,
  pieceGeneration: PieceGenerationConfig = PieceGenerationConfig.default,
  scoring: ScoringConfig = ScoringConfig.default,
  levelConfig: LevelConfig = LevelConfig.default,
  initialLevel: Int = 1,
  allowHold: Boolean = true,
  allowHardDrop: Boolean = true,
  previewCount: Int = 1,
  gravityEnabled: Boolean = true
):
  def validate: Either[ValidationError, VariantConfig]
  def withEnabledPieces(pieces): VariantConfig
  def withBoardSize(w, h): VariantConfig
  def withGeneration(cfg): VariantConfig
  def withScoring(cfg): VariantConfig
  def withLevelConfig(cfg): VariantConfig
  def withInitialLevel(l): VariantConfig
  def withHold(b): VariantConfig
  def withHardDrop(b): VariantConfig
  def withPreviewCount(n): VariantConfig
  def withGravity(b): VariantConfig
```

**Predefined Variants:**
```scala
object VariantConfig:
  val default: VariantConfig
  def classic: VariantConfig
  def extended: VariantConfig
  def builder(name: String): VariantConfigBuilder
```

### `ScoringConfig`

```scala
case class ScoringConfig(
  singleLine: Int = 100,
  doubleLine: Int = 300,
  tripleLine: Int = 500,
  tetrisLine: Int = 800,
  softDropPerCell: Int = 1,
  hardDropPerCell: Int = 2,
  levelMultiplier: Boolean = true
):
  def scoreForLines(lines: Int, level: Int): Int
  def softDropScore(cells: Int): Int
  def hardDropScore(cells: Int): Int
```

### `LevelConfig`

```scala
case class LevelConfig(
  linesPerLevel: Int = 10,
  initialDropInterval: Int = 1000,
  minDropInterval: Int = 50,
  dropIntervalDecrease: Int = 50,
  maxLevel: Int = 20
):
  def dropIntervalForLevel(level: Int): Int
  def levelForLines(linesCleared: Int): Int
  def linesForLevel(level: Int): Int
```

### `PieceGenerationConfig`

```scala
case class PieceGenerationConfig(
  strategy: GenerationStrategy = GenerationStrategy.Weighted,
  weights: Map[PieceId, Int] = Map.empty,
  seed: Option[Long] = None,
  bagSize: Int = 7
):
  def weightFor(id: PieceId): Int
  def hasWeights: Boolean
  def withWeight(id, weight): PieceGenerationConfig
  def withoutWeight(id): PieceGenerationConfig
  def withStrategy(s): PieceGenerationConfig
  def withSeed(seed): PieceGenerationConfig
```

### `GenerationStrategy`

```scala
enum GenerationStrategy:
  case Uniform
  case Weighted
  case Bag(size: Int = 7)
  case Custom(className: String)
```

### `ValidationError`

```scala
case class ValidationError(message: String) extends Exception(message)
```

---

## Package: `extetris.generation`

### `RandomState`

```scala
case class RandomState(seed: Long, state: Long):
  def nextInt(bound: Int): (Int, RandomState)
  def nextLong(): (Long, RandomState)
  def nextDouble(): (Double, RandomState)
  def nextBoolean(): (Boolean, RandomState)
  def setSeed(newSeed: Long): RandomState
  def currentSeed: Long
  def currentState: Long

object RandomState:
  def fromSeed(seed: Long): RandomState
  def random(): RandomState
```

### `PieceGenerator`

```scala
trait PieceGenerator:
  def nextPiece(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState): (Option[PieceId], RandomState)
  def previewPieces(registry: PieceRegistry, config: PieceGenerationConfig, randomState: RandomState, count: Int): (List[PieceId], RandomState)

object PieceGenerator:
  def forStrategy(strategy: GenerationStrategy): PieceGenerator
```

**Implementations:**
- `UniformGenerator`
- `WeightedGenerator`
- `BagGenerator(size: Int)`

---

## Package: `extetris.engine`

### `GameEngine`

```scala
object GameEngine:
  def step(state: GameState, input: GameInput): (GameState, List[GameEvent])
```

---

## Package: `extetris.persistence`

### `GameSerializer`

```scala
object GameSerializer:
  def serialize(state: GameState): String
  def deserialize(json: String): Either[io.circe.Error, GameState]
```

### `GamePersistence`

```scala
object GamePersistence:
  def save(state: GameState, path: String): Either[Exception, Unit]
  def load(path: String): Either[Exception, GameState]
  def saveToFile(state: GameState, file: File): Either[Exception, Unit]
  def loadFromFile(file: File): Either[Exception, GameState]
```

---

## Package: `extetris.replay`

### `Replay`

```scala
case class Replay(
  version: Int = 1,
  config: VariantConfig,
  initialRandomState: RandomState,
  inputs: List[GameInput],
  events: List[GameEvent] = List.empty
):
  def addInput(input: GameInput): Replay
  def addEvent(event: GameEvent): Replay
  def withEvents(newEvents: List[GameEvent]): Replay

object Replay:
  def record(initialState: GameState, inputs: List[GameInput]): Replay
  def empty(config: VariantConfig, seed: Long): Replay
  def serialize(replay: Replay): String
  def deserialize(json: String): Either[io.circe.Error, Replay]
  def save(replay: Replay, path: String): Either[Exception, Unit]
  def load(path: String): Either[Exception, Replay]
```

### `ReplayPlayer`

```scala
object ReplayPlayer:
  def play(replay: Replay): List[(GameState, GameInput, List[GameEvent], GameState)]
  def playStepByStep(replay: Replay): Iterator[(GameState, GameInput, List[GameEvent], GameState)]
  def verify(replay: Replay): Boolean
```

---

## Package: `extetris.runner`

### `GameRunner`

```scala
class GameRunner(initialState: GameState)(using ExecutionContext):
  def state: GameState
  def isRunning: Boolean
  def subscribe(listener: GameState => Unit): () => Unit
  def start(): Unit
  def stop(): Unit
  def pause(): Unit
  def resume(): Unit
  def restart(): Unit
  def handleInput(input: GameInput): Unit
  def changeConfig(config: VariantConfig): Unit
  def save(path: String): Either[Exception, Unit]
  def load(path: String): Either[Exception, Unit]

object GameRunner:
  def apply(config: VariantConfig, seed: Option[Long] = None)(using EC): GameRunner
  def fromState(state: GameState)(using EC): GameRunner
```

---

## Package: `extetris.examples`

### `ExampleVariants`

```scala
object ExampleVariants:
  val classic: VariantConfig
  val extended: VariantConfig
  val mini: VariantConfig
  val giant: VariantConfig
  val challenge: VariantConfig
  val customPieceVariant: VariantConfig
  val all: Map[String, VariantConfig]
  def runExample(name: String): Unit
```