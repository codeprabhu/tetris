# ExTetris Configuration Guide

## JSON Schema

All variant configurations can be expressed as JSON. This is the complete schema.

### Root Object: VariantConfig

```json
{
  "name": "string",
  "boardWidth": "integer (4-50)",
  "boardHeight": "integer (4-100)",
  "enabledPieces": ["PieceId"],
  "pieceGeneration": "PieceGenerationConfig",
  "scoring": "ScoringConfig",
  "levelConfig": "LevelConfig",
  "initialLevel": "integer (1-maxLevel)",
  "allowHold": "boolean",
  "allowHardDrop": "boolean",
  "previewCount": "integer (0-5)",
  "gravityEnabled": "boolean"
}
```

**Required fields:** `name`  
**Optional fields:** All others have defaults.

---

### PieceId Values

Built-in piece identifiers:

| ID | Description |
|----|-------------|
| `"I"` | 4×1 line |
| `"O"` | 2×2 square |
| `"T"` | T-shape |
| `"S"` | S-shape |
| `"Z"` | Z-shape |
| `"J"` | J-shape |
| `"L"` | L-shape |
| `"Single"` | 1×1 cell |
| `"L3"` | 3-cell L-shape |
| `"Plus"` | 5-cell plus-shape |

Custom pieces use their defined ID string.

---

### PieceGenerationConfig

```json
{
  "strategy": "GenerationStrategy",
  "weights": { "PieceId": "integer (≥0)" },
  "seed": "integer (optional)",
  "bagSize": "integer (default: 7)"
}
```

**GenerationStrategy** (one of):

```json
{ "type": "Uniform" }
{ "type": "Weighted" }
{ "type": "Bag", "size": 7 }
{ "type": "Custom", "className": "fully.qualified.ClassName" }
```

**Weight Rules:**
- Missing piece IDs default to weight 1
- Weight 0 = never selected
- Negative weights = validation error
- Disabled pieces never generated

---

### ScoringConfig

```json
{
  "singleLine": "integer (default: 100)",
  "doubleLine": "integer (default: 300)",
  "tripleLine": "integer (default: 500)",
  "tetrisLine": "integer (default: 800)",
  "softDropPerCell": "integer (default: 1)",
  "hardDropPerCell": "integer (default: 2)",
  "levelMultiplier": "boolean (default: true)"
}
```

---

### LevelConfig

```json
{
  "linesPerLevel": "integer (default: 10)",
  "initialDropInterval": "integer (default: 1000)",
  "minDropInterval": "integer (default: 50)",
  "dropIntervalDecrease": "integer (default: 50)",
  "maxLevel": "integer (default: 20)"
}
```

**Drop Interval Calculation:**
```
dropInterval = max(minDropInterval, initialDropInterval - (level - 1) * dropIntervalDecrease)
```

**Level Calculation:**
```
level = 1 + linesCleared / linesPerLevel
```

---

## Complete Example

```json
{
  "name": "Custom Variant",
  "boardWidth": 10,
  "boardHeight": 20,
  "enabledPieces": ["I", "O", "T", "S", "Z", "J", "L", "Single", "L3", "Plus"],
  "pieceGeneration": {
    "strategy": { "type": "Weighted" },
    "weights": {
      "I": 3, "O": 2, "T": 3,
      "S": 2, "Z": 2, "J": 3, "L": 3,
      "Single": 1, "L3": 2, "Plus": 1
    },
    "seed": 12345,
    "bagSize": 7
  },
  "scoring": {
    "singleLine": 100,
    "doubleLine": 300,
    "tripleLine": 500,
    "tetrisLine": 800,
    "softDropPerCell": 1,
    "hardDropPerCell": 2,
    "levelMultiplier": true
  },
  "levelConfig": {
    "linesPerLevel": 10,
    "initialDropInterval": 1000,
    "minDropInterval": 50,
    "dropIntervalDecrease": 50,
    "maxLevel": 20
  },
  "initialLevel": 1,
  "allowHold": true,
  "allowHardDrop": true,
  "previewCount": 3,
  "gravityEnabled": true
}
```

---

## Predefined Variants

### Classic ExTetris (`VariantConfig.classic`)

```json
{
  "name": "Classic ExTetris",
  "boardWidth": 10,
  "boardHeight": 20,
  "enabledPieces": ["I", "O", "T", "S", "Z", "J", "L"],
  "pieceGeneration": { "strategy": { "type": "Uniform" } },
  "scoring": {
    "singleLine": 40, "doubleLine": 100, "tripleLine": 300, "tetrisLine": 1200,
    "softDropPerCell": 1, "hardDropPerCell": 2, "levelMultiplier": true
  },
  "levelConfig": { "linesPerLevel": 10, "initialDropInterval": 1000, "minDropInterval": 50, "dropIntervalDecrease": 50, "maxLevel": 20 },
  "initialLevel": 1,
  "allowHold": false,
  "allowHardDrop": false,
  "previewCount": 1,
  "gravityEnabled": true
}
```

### Extended ExTetris (`VariantConfig.extended`)

```json
{
  "name": "Extended ExTetris",
  "boardWidth": 10,
  "boardHeight": 20,
  "enabledPieces": ["I", "O", "T", "S", "Z", "J", "L", "Single", "L3", "Plus"],
  "pieceGeneration": {
    "strategy": { "type": "Weighted" },
    "weights": {
      "I": 3, "O": 2, "T": 3, "S": 2, "Z": 2, "J": 3, "L": 3,
      "Single": 1, "L3": 2, "Plus": 1
    }
  },
  "scoring": { "singleLine": 100, "doubleLine": 300, "tripleLine": 500, "tetrisLine": 800, "softDropPerCell": 1, "hardDropPerCell": 2, "levelMultiplier": true },
  "levelConfig": { "linesPerLevel": 5, "initialDropInterval": 800, "minDropInterval": 30, "dropIntervalDecrease": 60, "maxLevel": 30 },
  "initialLevel": 1,
  "allowHold": true,
  "allowHardDrop": true,
  "previewCount": 3,
  "gravityEnabled": true
}
```

---

## Validation Rules

| Rule | Error Message |
|------|---------------|
| `boardWidth < 4` | "Board width must be at least 4" |
| `boardWidth > 50` | "Board width must be at most 50" |
| `boardHeight < 4` | "Board height must be at least 4" |
| `boardHeight > 100` | "Board height must be at most 100" |
| `enabledPieces.empty` | "At least one piece must be enabled" |
| `weight < 0` | "Weight for piece 'X' must be non-negative" |
| `initialLevel < 1` | "Initial level must be at least 1" |
| `initialLevel > maxLevel` | "Initial level cannot exceed max level" |
| `previewCount < 0` | "Preview count must be non-negative" |
| `previewCount > 5` | "Preview count must be at most 5" |

---

## Loading Configuration in Code

### From File

```scala
import io.circe.parser.decode
import extetris.config.VariantConfig
import scala.io.Source

val json = Source.fromFile("variant.json").mkString
decode[VariantConfig](json) match
  case Right(config) =>
    config.validate match
      case Right(valid) => ExTetrisAPI.newGame(valid, Some(12345L))
      case Left(e) => println(s"Invalid: ${e.message}")
  case Left(e) => println(s"Parse error: ${e.getMessage}")
```

### From String

```scala
import io.circe.parser.decode
import extetris.config.VariantConfig

val jsonString = """{"name": "Test", "boardWidth": 10, ...}"""
decode[VariantConfig](jsonString).foreach(_.validate.foreach(ExTetrisAPI.newGame(_, None)))
```

---

## Saving Configuration

```scala
import extetris.config.VariantConfig
import io.circe.syntax._

val config = VariantConfig.extended
val json = config.asJson.spaces2  // Pretty print
// Or: config.asJson.noSpaces for compact

import java.nio.file.Files
Files.writeString(java.nio.file.Paths.get("my-variant.json"), json)
```

---

## Environment Variable Override

For seed configuration, you can use environment variables:

```scala
val seed = sys.env.get("EXTETRIS_SEED").map(_.toLong)
val state = ExTetrisAPI.newGame(config, seed)
```

---

## Migration Notes

### Version 1.0 (Current)

- All fields as documented above
- `bagSize` in PieceGenerationConfig defaults to 7
- `previewCount` max is 5

### Future Versions

- May add new fields with defaults (backward compatible)
- May add new GenerationStrategy types
- Breaking changes will increment version field