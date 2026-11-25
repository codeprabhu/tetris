# Known Limitations

This document lists features that are not implemented, deliberate simplifications, and areas for future improvement.

## Not Implemented

### Advanced Game Mechanics
- **Super Rotation System (SRS)**: Wall kicks use simplified offsets only. True SRS with floor kicks and specific rotation tables not implemented.
- **T-Spin Detection**: No special scoring or detection for T-spins.
- **Combo System**: No combo counter or bonus for consecutive line clears.
- **All Clear / Perfect Clear**: No detection or bonus for clearing the entire board.
- **Hold Piece Preview**: Hold shows piece type but not its rotation state.
- **Multiplayer**: No multiplayer support (VS, co-op, or online).

### Piece System
- **Custom Rotation Tables**: Cannot define per-rotation cell offsets; only rotation behavior (Full/Limited/None) supported.
- **Piece Metadata**: No support for piece-specific properties (e.g., "this piece cannot be held", "this piece scores double").
- **Piece Animations**: No piece entry animations or smooth rotation interpolation.

### Generation
- **True Bag Randomizer**: `BagGenerator` currently falls back to weighted selection. A proper 7-bag (or 14-bag) implementation that guarantees all pieces appear before repeating is not implemented.
- **History-Based Generation**: No "no repeat last N pieces" or drought protection.

### Scoring & Levels
- **Dynamic Scoring Events**: Scoring only triggers on line clears and drops. No scoring for: spin bonuses, back-to-back, combos, perfect clears.
- **Variable Level Curves**: Level progression is linear (fixed lines per level). No support for curves, plateaus, or custom functions.
- **Speed Curves**: Drop interval decreases linearly. No support for gravity curves (e.g., 20G, step curves).

### Persistence & Replay
- **Replay Compression**: Replays store full input sequence; no compression or delta encoding.
- **Replay Metadata**: No player name, timestamp, duration, or checksum in replay files.
- **Save Versioning**: No explicit version field in save files for migration.

### GUI
- **Settings Persistence**: SettingsScene controls not connected to actual variant configuration.
- **Key Binding Customization**: Controls hardcoded; no rebind support.
- **Themes/Skins**: No theme system for board/piece colors.
- **High Score Table**: No persistent high score storage.
- **Pause Menu**: Pause shows an overlay and resumes with P (or Esc to the menu); there is no button-driven pause menu.
- **Touch/Mobile Support**: Desktop-only; no touch controls.
- **Accessibility**: No screen reader support, colorblind modes, or high contrast option.

### Testing
- **Property-Based Testing**: Only example-based tests; no ScalaCheck integration.
- **Integration Tests**: No end-to-end GUI tests.
- **Performance Benchmarks**: No benchmarks for engine throughput or memory usage.

## Deliberate Simplifications

### Architecture
- **Single-Threaded Engine**: Game logic runs on one thread. No parallelism for AI, physics, or rendering.
- **No Dependency Injection**: Components instantiated directly; no DI framework.
- **No Plugin System**: Custom generation strategies require recompilation (class name in config).

### Domain Model
- **No Sub-Cell Precision**: All positions are integer grid coordinates. No sub-cell movement for smooth animation.
- **Immutable State Only**: No mutable state option for performance-critical contexts.
- **No Command Pattern**: Inputs are simple enums; no undo/redo or command history beyond replay.

### Configuration
- **No Runtime Variant Discovery**: Variants must be known at compile time or loaded from explicit JSON files.
- **No Schema Validation UI**: JSON config errors only shown at load time, not in editor.

## Areas for Improvement

### Performance
- Board representation uses `Vector[Vector[Cell]]` - could use flat array or bitboards for speed.
- RandomState creates new `scala.util.Random` on each call - could use faster RNG (XorShift, PCG).
- Piece collision checks iterate all cells - could use bounding box early-out.

### Code Quality
- Some `var` usage in GameRunner for internal state - could be fully immutable with actor model.
- Wall kick logic hardcoded - should be data-driven from piece definitions.
- SettingsScene not integrated - needs connection to VariantConfig builder.

### Extensibility
- Add `PieceModifier` trait for runtime piece behavior changes (e.g., "ice pieces slide", "heavy pieces fall faster").
- Add `GameRule` extension points for custom line clear conditions, spawn rules, etc.
- Add scripting API (Scala.js, Lua) for variant logic without JVM recompilation.

## Future Roadmap (Post-Assignment)

1. **SRS Implementation**: Full wall kick tables per piece
2. **Bag Randomizer**: Proper 7-bag / 14-bag with configurable size
3. **Replay Compression**: Delta encoding + gzip
4. **High Score System**: Local + optional online leaderboard
5. **Theme Engine**: JSON-based skinning
6. **Key Rebinding**: Configurable controls
7. **Property Tests**: ScalaCheck for engine invariants
8. **Benchmarks**: JMH suite for engine performance
9. **Web Demo**: Scala.js compile to browser

## Assignment Scope Notes

The following were **required** and **implemented**:
- ✅ Core engine with pure state transitions
- ✅ 7 standard tetrominoes + 3 custom pieces
- ✅ Configurable board dimensions
- ✅ Configurable scoring & level progression
- ✅ Weighted & seeded piece generation
- ✅ Deterministic execution
- ✅ Dynamic config changes
- ✅ Save/load with JSON
- ✅ Replay recording & deterministic playback
- ✅ Playable ScalaFX GUI
- ✅ Public API for variant creation
- ✅ Two working example variants (Classic, Extended)
- ✅ Comprehensive test suite
- ✅ Documentation (README, Arch, API, Manual, Config, UML)

The following were **not required** and **not implemented**:
- SRS, T-spins, combos
- Multiplayer, AI, plugins
- High scores, themes, key rebinding
- Property-based testing, benchmarks