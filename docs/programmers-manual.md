# ExTetris Platform - Game Programmer's Manual

This manual guides you through creating your own Tetris variants using the ExTetris Platform API.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Creating a Custom Piece](#creating-a-custom-piece)
3. [Defining a Variant Configuration](#defining-a-variant-configuration)
4. [Configuring Piece Weights](#configuring-piece-weights)
5. [Configuring Scoring](#configuring-scoring)
6. [Configuring Level Progression](#configuring-level-progression)
7. [Running a Game Programmatically](#running-a-game-programmatically)
8. [Saving and Loading Games](#saving-and-loading-games)
9. [Recording and Playing Replays](#recording-and-playing-replays)
10. [Complete Example](#complete-example)

---

## Quick Start

```scala
import extetris.api.ExTetrisAPI
import extetris.config.VariantConfig

// Use a built-in variant
val state = ExTetrisAPI.newGame(ExTetrisAPI.extendedVariant, Some(12345L))

// Game loop
var currentState = state
while currentState.isPlaying do
  // Get user input somehow...
  val input = getUserInput()
  val (nextState, events) = ExTetrisAPI.applyInput(currentState, input)
  currentState = nextState
  // Render currentState...
```

---

## Creating a Custom Piece

Pieces are defined by their cell offsets and rotation behavior.

### Standard Piece Factory

```scala
import extetris.api.ExTetrisAPI
import extetris.pieces.{PieceId, PieceColor, RotationBehavior}
import extetris.model.Position

// Standard piece with automatic rotation generation
val myPiece = ExTetrisAPI.defineStandardPiece(
  id = PieceId("MyPiece"),
  name = "My Piece",
  color = PieceColor(1.0, 0.5, 0.0),  // Orange
  cells = Set(
    Position(0, 0), Position(1, 0), Position(2, 0),  // Horizontal line of 3
    Position(1, 1)                                     // Center block below
  ),
  behavior = RotationBehavior.Full  // 4 rotation states
)
```

### Custom Piece Factory (Full Control)

```scala
// Custom piece with manually specified rotations
val complexPiece = ExTetrisAPI.defineCustomPiece(
  id = PieceId("Complex"),
  name = "Complex Piece",
  color = PieceColor(0.5, 0.0, 1.0),  // Purple
  cells = Set(Position(0, 0), Position(1, 0), Position(2, 0)),  // Base shape
  behavior = RotationBehavior.Limited(2)  // Only 2 distinct rotations
)
```

### Rotation Behaviors

| Behavior | Description | Use Case |
|----------|-------------|----------|
| `Full` | 4 rotations (0°, 90°, 180°, 270°) | Asymmetric pieces (T, J, L) |
| `Limited(n)` | n distinct rotations | Symmetric pieces (I, S, Z) |
| `None` | No rotation | Square pieces (O), single cell |

### Piece Geometry Guidelines

1. **Origin**: Pieces are normalized so minimum x,y = 0
2. **Rotation Center**: Calculated as center of bounding box
3. **Cell Coordinates**: Use integer grid positions
4. **No Overlapping Cells**: Each cell position must be unique

---

## Defining a Variant Configuration

### Using the Builder (Recommended)

```scala
import extetris.config.VariantConfig
import extetris.pieces.PieceId

val myVariant = VariantConfig.builder("My Awesome Tetris")
  .boardSize(12, 24)                    // Width, Height
  .enabledPieces(PieceId.standardIds + PieceId("MyPiece"))
  .pieceGeneration(
    ExTetrisAPI.generationConfig(
      strategy = extetris.config.GenerationStrategy.Weighted,
      weights = Map(PieceId("MyPiece") -> 10) ++ PieceId.standardIds.map(_ -> 1).toMap,
      seed = Some(42L)
    )
  )
  .scoring(ExTetrisAPI.scoringConfig(tetris = 2000))
  .levelConfig(ExTetrisAPI.levelConfig(linesPerLevel = 15))
  .initialLevel(1)
  .allowHold(true)
  .allowHardDrop(true)
  .previewCount(3)
  .gravity(true)
  .build()

// Validate before use
myVariant.validate match
  case Right(validConfig) => println("Config valid!")
  case Left(error) => println(s"Invalid: ${error.message}")
```

### Direct Construction

```scala
val config = VariantConfig(
  name = "Direct Config",
  boardWidth = 10,
  boardHeight = 20,
  enabledPieces = Set(PieceId.I, PieceId.O, PieceId.T),
  // ... other fields with defaults
)
```

### Loading from JSON

```scala
import io.circe.parser.decode
import extetris.config.VariantConfig

val json = scala.io.Source.fromFile("my-variant.json").mkString
decode[VariantConfig](json) match
  case Right(config) =>
    config.validate match
      case Right(valid) => ExTetrisAPI.newGame(valid, Some(12345L))
      case Left(e) => println(s"Validation failed: ${e.message}")
  case Left(e) => println(s"Parse failed: ${e.getMessage}")
```

---

## Configuring Piece Weights

Weights control relative probability in `Weighted` generation strategy.

```scala
import extetris.config.PieceGenerationConfig
import extetris.config.GenerationStrategy
import extetris.pieces.PieceId

// Equal weights (uniform-like)
val uniformWeights = PieceId.allBuiltInIds.map(_ -> 1).toMap

// Favor certain pieces
val weightedConfig = PieceGenerationConfig(
  strategy = GenerationStrategy.Weighted,
  weights = Map(
    PieceId.I -> 1,      // Rare
    PieceId.O -> 2,
    PieceId.T -> 3,
    PieceId.S -> 2,
    PieceId.Z -> 2,
    PieceId.J -> 3,
    PieceId.L -> 3,
    PieceId.Single -> 5, // Common
    PieceId.L3 -> 4,
    PieceId.Plus -> 1    // Rare
  ),
  seed = Some(12345L)
)
```

**Rules:**
- Weight must be ≥ 0
- Weight 0 = never selected
- Missing pieces get default weight 1
- Disabled pieces never generated regardless of weight

---

## Configuring Scoring

```scala
import extetris.api.ExTetrisAPI

// Classic NES scoring
val classicScoring = ExTetrisAPI.scoringConfig(
  single = 40,
  double = 100,
  triple = 300,
  tetris = 1200,
  softDrop = 1,
  hardDrop = 2,
  levelMultiplier = true
)

// Modern guideline scoring
val modernScoring = ExTetrisAPI.scoringConfig(
  single = 100,
  double = 300,
  triple = 500,
  tetris = 800,
  softDrop = 1,
  hardDrop = 2,
  levelMultiplier = true
)

// Custom scoring
val customScoring = ExTetrisAPI.scoringConfig(
  single = 50,
  double = 150,
  triple = 400,
  tetris = 1000,
  softDrop = 2,
  hardDrop = 3,
  levelMultiplier = false  // Flat scoring
)
```

**Score Calculation:**
```
lineScore = baseScore[lines] * (levelMultiplier ? level : 1)
softDropScore = cellsDropped * softDropPerCell
hardDropScore = cellsDropped * hardDropPerCell
```

---

## Configuring Level Progression

```scala
import extetris.api.ExTetrisAPI

// Standard progression
val standardLevels = ExTetrisAPI.levelConfig(
  linesPerLevel = 10,
  initialDropInterval = 1000,  // ms at level 1
  minDropInterval = 50,        // ms minimum
  dropIntervalDecrease = 50,   // ms per level
  maxLevel = 20
)

// Fast progression (for speed runs)
val fastLevels = ExTetrisAPI.levelConfig(
  linesPerLevel = 5,
  initialDropInterval = 800,
  minDropInterval = 30,
  dropIntervalDecrease = 60,
  maxLevel = 30
)

// Slow progression (for beginners)
val slowLevels = ExTetrisAPI.levelConfig(
  linesPerLevel = 20,
  initialDropInterval = 1200,
  minDropInterval = 100,
  dropIntervalDecrease = 30,
  maxLevel = 15
)
```

**Drop Interval Formula:**
```
interval = max(minDropInterval, initialDropInterval - (level - 1) * decrease)
```

---

## Running a Game Programmatically

### Basic Game Loop

```scala
import extetris.api.ExTetrisAPI
import extetris.model.{GameState, GameInput, GameStatus}
import extetris.config.VariantConfig

def runGame(config: VariantConfig, seed: Long = System.nanoTime()): Unit =
  var state = ExTetrisAPI.newGame(config, Some(seed))
  
  while state.isPlaying do
    // In a real game, get input from user/UI
    val input = getNextInput()  // Your implementation
    
    val (newState, events) = ExTetrisAPI.applyInput(state, input)
    state = newState
    
    // Handle events
    events.foreach(handleEvent)
    
    // Render
    render(state)

def handleEvent(event: GameEvent): Unit = event match
  case GameEvent.LinesCleared(count, _) => println(s"Cleared $count lines!")
  case GameEvent.ScoreChanged(_, newScore) => println(s"Score: $newScore")
  case GameEvent.LevelChanged(_, newLevel) => println(s"Level up! Now level $newLevel")
  case GameEvent.GameOver(score, level, lines) => 
    println(s"GAME OVER - Score: $score, Level: $level, Lines: $lines")
  case _ => ()

def render(state: GameState): Unit =
  // Your rendering code here
  printBoard(state.board)
  printCurrentPiece(state.currentPiece)
```

### With GameRunner (Automatic Tick)

```scala
import extetris.runner.GameRunner
import scala.concurrent.ExecutionContext

given ExecutionContext = ExecutionContext.global

val runner = GameRunner(config, Some(seed))

runner.subscribe { state =>
  // Called on every state change
  Platform.runLater { () => updateUI(state) }
}

runner.start()
// ... user inputs via runner.handleInput(GameInput.MoveLeft) etc.
```

---

## Saving and Loading Games

### Save Current Game

```scala
import extetris.api.ExTetrisAPI

val state: GameState = // current game state
ExTetrisAPI.saveGame(state, "savegame.json") match
  case Right(_) => println("Saved!")
  case Left(e) => println(s"Save failed: ${e.getMessage}")
```

### Load Game

```scala
ExTetrisAPI.loadGame("savegame.json") match
  case Right(state) =>
    println("Loaded!")
    // Continue from state
  case Left(e) => println(s"Load failed: ${e.getMessage}")
```

### Serialize to String (for network, etc.)

```scala
val json: String = ExTetrisAPI.serializeGame(state)
// Send json over network...

val restored = ExTetrisAPI.deserializeGame(json)
```

---

## Recording and Playing Replays

### Record a Replay

```scala
import extetris.api.ExTetrisAPI
import extetris.model.{GameState, GameInput}

val initialState = ExTetrisAPI.newGame(config, Some(12345L))

// Play game, collecting inputs
val inputs = scala.collection.mutable.ListBuffer.empty[GameInput]
var state = initialState

while state.isPlaying do
  val input = getUserInput()
  inputs += input
  val (next, _) = ExTetrisAPI.applyInput(state, input)
  state = next

// Create replay
val replay = ExTetrisAPI.recordReplay(initialState, inputs.toList)

// Save replay
import extetris.replay.Replay
Replay.save(replay, "replay.json")
```

### Play a Replay

```scala
import extetris.api.ExTetrisAPI

val replay = Replay.load("replay.json").getOrElse(throw Exception("Load failed"))
val results = ExTetrisAPI.playReplay(replay)

// results = List[(stateBefore, input, events, stateAfter)]
results.foreach { case (before, input, events, after) =>
  println(s"Input: $input, Events: ${events.size}")
  // Render 'after' state for visualization
}
```

### Step-by-Step Replay

```scala
import extetris.replay.ReplayPlayer

val iterator = ReplayPlayer.playStepByStep(replay)
while iterator.hasNext do
  val (before, input, events, after) = iterator.next()
  render(after)
  Thread.sleep(100)  // Control playback speed
```

### Verify Replay Determinism

```scala
if ReplayPlayer.verify(replay) then
  println("Replay verified: deterministic reproduction successful")
else
  println("Replay verification FAILED: non-deterministic behavior detected")
```

---

## Complete Example: "Pentomino Tetris"

Here's a complete, runnable example creating a variant with 5-cell pieces (pentominoes).

```scala
package myvariants

import extetris.api.ExTetrisAPI
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig, PieceGenerationConfig, GenerationStrategy}
import extetris.pieces.{PieceId, PieceColor, PieceDefinition, RotationBehavior}
import extetris.model.Position
import extetris.runner.GameRunner
import scala.concurrent.ExecutionContext

object PentominoTetris:
  
  // Define all 12 pentominoes (5-cell pieces)
  private val pentominoes: Map[PieceId, PieceDefinition] = Map(
    PieceId("F") -> define("F", 0xFF0000, Set((1,0),(2,0),(0,1),(1,1),(1,2))),
    PieceId("I") -> define("I", 0x00FFFF, Set((0,0),(0,1),(0,2),(0,3),(0,4)), RotationBehavior.Limited(2)),
    PieceId("L") -> define("L", 0xFFA500, Set((0,0),(0,1),(0,2),(0,3),(1,0))),
    PieceId("N") -> define("N", 0x00FF00, Set((1,0),(1,1),(0,1),(0,2),(0,3))),
    PieceId("P") -> define("P", 0xFF00FF, Set((0,0),(1,0),(0,1),(1,1),(0,2))),
    PieceId("T") -> define("T", 0x800080, Set((0,0),(1,0),(2,0),(1,1),(1,2))),
    PieceId("U") -> define("U", 0x0000FF, Set((0,0),(1,0),(0,1),(1,1),(0,2))), // Same as P but different rotations
    PieceId("V") -> define("V", 0xFFFF00, Set((0,0),(0,1),(0,2),(1,2),(2,2))),
    PieceId("W") -> define("W", 0x00FFFF, Set((0,0),(0,1),(1,1),(1,2),(2,2))),
    PieceId("X") -> define("X", 0xFF00FF, Set((1,0),(0,1),(1,1),(2,1),(1,2))),
    PieceId("Y") -> define("Y", 0xFFA500, Set((0,0),(1,0),(2,0),(3,0),(1,1))),
    PieceId("Z") -> define("Z", 0x800080, Set((0,0),(1,0),(1,1),(1,2),(2,2)))
  )
  
  private def define(name: String, colorInt: Int, cells: Set[(Int,Int)], behavior: RotationBehavior = RotationBehavior.Full): PieceDefinition =
    val pieceId = PieceId(name)
    val color = PieceColor(
      ((colorInt >> 16) & 0xFF) / 255.0,
      ((colorInt >> 8) & 0xFF) / 255.0,
      (colorInt & 0xFF) / 255.0
    )
    val positions = cells.map((x,y) => Position(x,y))
    ExTetrisAPI.defineCustomPiece(pieceId, name, color, positions, behavior)
  
  // Create variant configuration
  val config: VariantConfig = VariantConfig.builder("Pentomino Tetris")
    .boardSize(12, 24)
    .enabledPieces(pentominoes.keys.toSet)
    .pieceGeneration(PieceGenerationConfig(
      strategy = GenerationStrategy.Weighted,
      weights = pentominoes.keys.map(_ -> 1).toMap,
      seed = Some(12345L)
    ))
    .scoring(ScoringConfig(
      singleLine = 100,
      doubleLine = 300,
      tripleLine = 500,
      tetrisLine = 800,
      softDropPerCell = 1,
      hardDropPerCell = 2,
      levelMultiplier = true
    ))
    .levelConfig(LevelConfig(
      linesPerLevel = 10,
      initialDropInterval = 1000,
      minDropInterval = 50,
      dropIntervalDecrease = 50,
      maxLevel = 20
    ))
    .initialLevel(1)
    .allowHold(true)
    .allowHardDrop(true)
    .previewCount(3)
    .build()
  
  // Main entry point
  @main def run(): Unit =
    given ExecutionContext = ExecutionContext.global
    
    // Verify config
    config.validate match
      case Left(e) => println(s"Invalid config: ${e.message}"); return
      case Right(_) => println("Config valid!")
    
    // Create runner and start
    val runner = GameRunner(config, Some(12345L))
    
    // Subscribe to state changes (for UI)
    runner.subscribe { state =>
      println(s"Score: ${state.score}, Level: ${state.level}, Lines: ${state.linesCleared}")
    }
    
    runner.start()
    
    // In a real app, you'd connect this to a GUI
    // For demo, just run a few ticks
    Thread.sleep(5000)
    runner.stop()
    println("Demo complete!")
```

---

## Best Practices

1. **Always validate configs** before starting a game
2. **Use fixed seeds** for testing and reproducible demos
3. **Test edge cases**: piece at boundaries, rotation kicks, game over
4. **Keep pieces reasonable**: max 10 cells, avoid extremely long pieces
5. **Document your variant**: explain rules, scoring, and special mechanics
6. **Share as JSON**: makes variants portable and editable without recompilation

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| "Piece not found" | Ensure piece ID is in `enabledPieces` and registry |
| "Invalid config" | Check `config.validate` for specific error message |
| "Game over immediately" | Spawn position may be blocked; check board width vs piece width |
| "Replay doesn't match" | Ensure same seed, config, and input sequence |
| "Custom piece looks wrong" | Verify cell coordinates and rotation behavior |

---

## Next Steps

- Explore `extetris.examples.ExampleVariants` for more variants
- Read [API Reference](api.md) for complete method signatures
- Check [Architecture](architecture.md) for internal design details
- Look at [Configuration Guide](configuration.md) for JSON schema