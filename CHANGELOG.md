# Changelog — Documentation First (IntelliJ)

## [1.0.1] — 2026-05-21

### New features
- **Steps retrospective** — when a step is marked done in "New Task", a retrospective section is automatically appended to the corresponding `steps/<slug>.md` file
- **Spec file auto-created** — a `tasks/specification/spec-<slug>.md` is created at every init, new vision, and new task
- **Selective spec deletion** — "New Task" now shows a multi-select list of non-permanent spec files; only selected ones are deleted
- **Quick init mode** — new choice at init: Full (5 steps) or Quick (profile + context + first task)
- **CONTRACT session protocol** — generated `CONTRACT.md` now includes a session close protocol with 4 auto-triggers for the agent
- **New Vision and New Task actions** now registered in `plugin.xml` menus
- All UI labels translated to English

### Fixes
- `plugin.xml` description updated: removed obsolete `history.log` reference (now `history.json`)
- `change-notes` added to `plugin.xml` (required for JetBrains marketplace)

## [1.0.0] — 2026-04-30

### Initial public release
- Full DDD v2 lifecycle: init → vision → steps → tasks → done
- Agent-agnostic: works with JetBrains AI, GitHub Copilot, Cursor, or any assistant
- Zero external dependencies — pure Markdown + Git
- DDD tool window: tree view of all `.ai_context/` files
- Permanent files (`permanent-*`) survive all context resets
- Task history in `history.json` (JSON Lines, append-only)

