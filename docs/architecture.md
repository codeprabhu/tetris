# ExTetris Platform Architecture

## Overview

ExTetris follows a **Functional-OO architecture** with clear boundaries between:

- **Functional Core**: Pure game logic, immutable state transitions
- **Object-Oriented Extensibility**: Configuration, extension points, integration

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        ExTetris Platform                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │   Public API  │──│  Variant     │  │  Piece Registry      │  │
│  │  (ExTetrisAPI) │  │  Config      │  │  (PieceRegistry)     │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘  │
│         │                 │                     │              │
│         ▼                 ▼                     ▼              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Game Runner                            │  │
│  │  (Input handling, game loop, GUI coordination)           │  │
│  └──────────────────────────┬───────────────────────────────┘  │
│                             │                                   │
│         ┌───────────────────┼───────────────────┐              │
│         ▼                   ▼                   ▼              │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│  │ Game Engine │    │ Persistence │    │   Replay    │        │
│  │ (Pure Logic)│    │ (Serializer)│    │  (Player)   │        │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘        │
│         │                  │                  │                │
│         ▼                  ▼                  ▼                │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                      Domain Model                          │  │
│  │  GameState, Board, Piece, Position, Cell, Config, Event   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

```
User Input → GameRunner → GameEngine.step(state, input) → New GameState + Events
                                                              │
                                                              ▼
                                              GameRunner notifies listeners
                                                              │
                                                              ▼
                                                   GUI updates / Persistence
```

## Functional Core

### GameEngine

The engine implements pure state transitions:

```scala
def step(state: GameState, input: GameInput): (GameState, List[GameEvent])
```

**Properties:**
- No side effects
- Deterministic: same state + same input = same output
- Immutable: returns new GameState, never mutates
- Testable: can be unit tested without GUI or IO

### State Transitions

| Input | Transition |
|-------|------------|
| `MoveLeft/Right/Down` | Validate collision, update position |
| `RotateCW/CCW` | Validate rotation + wall kicks, update rotation |
| `HardDrop` | Drop to lock position, score, lock, clear lines, spawn next |
| `Hold` | Swap current with held piece (if allowed) |
| `Tick` | Advance drop counter, auto-drop when interval reached |
| `Pause/Resume/Restart` | Update game status |

## Object-Oriented Extensibility

### VariantConfig

Central configuration object defining all variant rules:

```scala
case class VariantConfig(
  name: String,
  boardWidth: Int,
  boardHeight: Int,
  enabledPieces: Set[PieceId],
  pieceGeneration: PieceGenerationConfig,
  scoring: ScoringConfig,
  levelConfig: LevelConfig,
  initialLevel: Int,
  allowHold: Boolean,
  allowHardDrop: Boolean,
  previewCount: Int,
  gravityEnabled: Boolean
)
```

**Validation**: All configs validated before game start with descriptive errors.

### Piece System

```
PieceDefinition (trait)
├── StandardPiece (7 tetrominoes)
└── CustomPiece (user-defined)

PieceDefinition contains:
- id, name, color
- rotations: Vector[PieceRotation]
- behavior: RotationBehavior (Full/Limited/None)

PieceRotation:
- cells: Set[Position]
- origin: Position (rotation center)
- rotateCW/CCW: Pure transformations
```

### Generation Strategy

```scala
trait PieceGenerator:
  def nextPiece(registry, config, randomState): (Option[PieceId], RandomState)
  def previewPieces(...): (List[PieceId], RandomState)

Implementations:
- UniformGenerator
- WeightedGenerator
- BagGenerator(size)
```

## Game State

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
  dropInterval: Int
)
```

**Key Design Decisions:**
- `randomState` stored for deterministic replay
- `config` embedded for self-contained saves
- No redundant derived fields (e.g., no `gameOver` boolean - use `status`)

## Board Representation

```scala
case class Board(width: Int, height: Int, grid: Vector[Vector[Cell]])

enum Cell:
  case Empty
  case Filled(pieceId: PieceId, color: PieceColor)
```

**Operations:**
- `canPlace(piece)`: Collision + boundary check
- `place(piece)`: Returns new board with piece locked
- `clearLines()`: Returns (newBoard, linesClearedCount)

## Persistence

### Serialization

Uses **Circe** for JSON encoding/decoding with:
- Custom codecs for all domain types
- Full round-trip fidelity
- Version tolerance (future-proof)

### Save Format

```json
{
  "board": { "width": 10, "height": 20, "grid": [...] },
  "currentPiece": { ... },
  "nextPiece": { ... },
  "heldPiece": { ... },
  "canHold": true,
  "score": 1234,
  "level": 3,
  "linesCleared": 15,
  "status": "Playing",
  "config": { ... },
  "randomState": { "seed": 12345, "state": 67890 },
  "dropCounter": 5,
  "dropInterval": 800
}
```

## Replay System

```
Replay = {
  version: Int,
  config: VariantConfig,
  initialRandomState: RandomState,
  inputs: List[GameInput],
  events: List[GameEvent]  // For verification
}

ReplayPlayer.play(replay) → List[(state, input, events, newState)]
```

**Verification**: `ReplayPlayer.verify(replay)` confirms deterministic reproduction.

## GUI Architecture

```
MainApp (JFXApp3)
  └── Stage
        ├── MainMenuScene
        ├── GameScene
        │     ├── BoardPane (renders Board + current piece)
        │     ├── NextPiecePane
        │     ├── HeldPiecePane
        │     ├── Score/Level/Lines labels
        │     └── Overlays (Pause, GameOver)
        └── SettingsScene
```

**Key Principle**: GUI only *consumes* the public API. No game logic in GUI code.

## Extension Points for Programmers

| Extension Point | How to Extend |
|-----------------|---------------|
| New piece | `ExTetrisAPI.defineCustomPiece(...)` + add to `enabledPieces` |
| New variant | `VariantConfig.builder(...).build()` or JSON file |
| New scoring | `ExTetrisAPI.scoringConfig(...)` |
| New level curve | `ExTetrisAPI.levelConfig(...)` |
| New generation | `PieceGenerationConfig(strategy = Custom("classname"))` |
| New rotation behavior | Use `RotationBehavior.Limited(n)` or `None` |

**No Engine Modification Required** for any of the above.

## Concurrency Model

- GameRunner runs on background thread (ExecutionContext)
- GUI updates via `Platform.runLater`
- GameState immutable - safe for concurrent reads
- Single-writer pattern: only GameRunner mutates its internal state reference

## Testing Strategy

| Layer | Approach |
|-------|----------|
| Domain | Property-based tests for Position, Rotation, Board |
| Engine | Deterministic sequence tests with fixed seeds |
| Config | Validation tests for all constraint combinations |
| Generation | Statistical tests for distribution + determinism |
| Persistence | Round-trip serialization tests |
| Replay | Verification tests with recorded events |
| API | Integration tests for custom variant creation |

## Known Limitations

1. No wall-kick system (SRS) - simplified kicks only
2. No multiplayer support
3. No AI/bot API
4. Bag generator simplified (uses weighted fallback)
5. No sound system
6. SettingsScene not fully connected to variant config