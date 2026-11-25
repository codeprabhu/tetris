# ExTetris Platform API

An extensible Tetris platform for creating, configuring, and running Tetris-like game variants. Built with Scala 3, ScalaFX, and Circe.

## Features

- **Extensible Game Engine**: Core mechanics implemented as pure state transitions
- **Configurable Piece System**: Standard tetrominoes + 3 custom pieces (Single, L3, Plus)
- **Variant Configuration**: Define game rules programmatically or via JSON
- **Multiple Generation Strategies**: Uniform, weighted, and bag-based piece selection
- **Deterministic Execution**: Seeded random generation for reproducible gameplay
- **Save/Load & Replay**: Full game state persistence and deterministic replay
- **Graphical Interface**: Playable ScalaFX GUI with menus, settings, NEXT/HOLD previews, ghost piece, pause overlay, and a dedicated game-over screen
- **Public API**: Clean extension points for creating custom variants

## Requirements

- JDK 17+
- sbt 1.9+

## Building

```bash
sbt compile
```

## Running Tests

```bash
sbt test
```

## Running the Application

```bash
sbt run
```

Or create an executable JAR:

```bash
sbt assembly
java -jar target/scala-3.3.3/extetris-platform-assembly-0.1.0-SNAPSHOT.jar
```

## Architecture

### Core Components

```
extetris/
├── model/          # Core domain types (Position, Cell, GameState, GameInput, GameEvent)
├── pieces/         # Piece system (definitions, rotations, registry)
├── board/          # Board mechanics (collision, placement, line clearing)
├── config/         # Variant configuration (scoring, levels, generation)
├── generation/     # Piece generation strategies
├── engine/         # Functional game engine (state transitions)
├── api/            # Public extension API
├── persistence/    # Serialization and save/load
├── replay/         # Replay recording and playback
├── runner/         # Game loop coordinator
├── gui/            # ScalaFX graphical interface
└── examples/       # Example variants
```

### Design Principles

1. **Functional Core**: Game logic is pure and deterministic
2. **OO Extensibility**: Configuration and extension points use traits/classes
3. **Immutable State**: GameState is immutable; transitions return new states
4. **Separation of Concerns**: Engine, GUI, persistence, and config are independent
5. **Programmer-Friendly**: New variants require no engine modifications

## Creating Custom Variants

### Programmatic Configuration

```scala
import extetris.api.ExTetrisAPI
import extetris.config.{VariantConfig, ScoringConfig, LevelConfig}
import extetris.pieces.{PieceId, PieceColor, RotationBehavior}
import extetris.model.Position

// Define a custom piece
val myPiece = ExTetrisAPI.defineCustomPiece(
  PieceId("MyPiece"), "My Piece", PieceColor(1.0, 0.5, 0.0),
  Set(Position(0, 0), Position(1, 0), Position(0, 1)),
  RotationBehavior.Full
)

// Create variant configuration
val config = VariantConfig.builder("My Variant")
  .boardSize(12, 24)
  .enabledPieces(PieceId.standardIds + PieceId("MyPiece"))
  .pieceGeneration(ExTetrisAPI.generationConfig(
    strategy = extetris.config.GenerationStrategy.Weighted,
    weights = Map(PieceId("MyPiece") -> 5) ++ PieceId.standardIds.map(_ -> 1).toMap
  ))
  .scoring(ExTetrisAPI.scoringConfig(tetris = 2000))
  .levelConfig(ExTetrisAPI.levelConfig(linesPerLevel = 15))
  .allowHold(true)
  .allowHardDrop(true)
  .previewCount(3)
  .build()

// Run the game
val state = ExTetrisAPI.newGame(config, Some(12345L))
```

### JSON Configuration

Save as `my-variant.json`:

```json
{
  "name": "My Variant",
  "boardWidth": 12,
  "boardHeight": 24,
  "enabledPieces": ["I", "O", "T", "S", "Z", "J", "L", "MyPiece"],
  "pieceGeneration": {
    "strategy": { "type": "Weighted" },
    "weights": { "MyPiece": 5, "I": 1, "O": 1, "T": 1, "S": 1, "Z": 1, "J": 1, "L": 1 },
    "seed": 12345,
    "bagSize": 7
  },
  "scoring": { "singleLine": 100, "doubleLine": 300, "tripleLine": 500, "tetrisLine": 2000 },
  "levelConfig": { "linesPerLevel": 15, "initialDropInterval": 1000 },
  "initialLevel": 1,
  "allowHold": true,
  "allowHardDrop": true,
  "previewCount": 3,
  "gravityEnabled": true
}
```

Load and play:

```scala
import io.circe.parser.decode
import extetris.config.VariantConfig

val json = scala.io.Source.fromFile("my-variant.json").mkString
decode[VariantConfig](json).foreach { config =>
  val state = ExTetrisAPI.newGame(config, Some(12345L))
  // ... run game
}
```

## Built-in Pieces

### Standard Tetrominoes
- **I**: 4×1 line (2 rotation states)
- **O**: 2×2 square (no rotation)
- **T**: T-shape (4 rotation states)
- **S**: S-shape (2 rotation states)
- **Z**: Z-shape (2 rotation states)
- **J**: J-shape (4 rotation states)
- **L**: L-shape (4 rotation states)

### Custom Pieces
- **Single**: 1×1 cell (no rotation)
- **L3**: 3-cell L-shape (4 rotation states)
- **Plus**: 5-cell plus-shape (4 rotation states)

## Generation Strategies

| Strategy | Description |
|----------|-------------|
| `Uniform` | Equal probability for all enabled pieces |
| `Weighted` | Probability proportional to configured weights |
| `Bag(size)` | Bag randomizer (draws all pieces before repeating) |

## Default Controls

| Key | Action |
|-----|--------|
| ← / → | Move left/right |
| ↑ | Rotate clockwise |
| ↓ | Soft drop |
| Space | Hard drop |
| C | Hold piece |
| P | Pause/Resume |

## Project Structure

```
src/
├── main/scala/extetris/     # Main source code
├── test/scala/extetris/     # Test suites
└── resources/               # Configuration examples
```

## Documentation

- [Architecture](docs/architecture.md)
- [API Reference](docs/api.md)
- [Programmer's Manual](docs/programmers-manual.md)
- [Configuration Guide](docs/configuration.md)
- [UML Diagrams](docs/uml/)

## License

Educational project for Principles of Programming Languages course.