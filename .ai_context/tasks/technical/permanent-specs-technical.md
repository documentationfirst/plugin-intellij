# DDD Plugin — JetBrains — Technical Specifications (v2)

See `documents/technical/permanent-specs-technical.md` for the legacy v1 spec.

## Key changes in v2

| Before (v1) | After (v2) |
|---|---|
| `documents/` | `tasks/` |
| `history.log` | `history.json` (JSON Lines) |
| init: profile + title + todos | init: profile + vision + title + steps + todos |
| no `vision.md` | `vision.md` created at init |
| no `steps/` | `steps/` created at init |

## Architecture source

See `tasks/specification/permanent-context-architecture.md` for the full `.ai_context/` spec.

