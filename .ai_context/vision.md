# Vision

*Created: 2026-05-15*

---

## Epic

Build the reference IDE extension for Documentation-Driven Development:
a lightweight, agent-agnostic JetBrains plugin that scaffolds and manages the `.ai_context/` folder,
turning project documentation into the primary interface between developers and AI agents.
Comparable feature parity with the VSCode plugin, adapted to the IntelliJ platform (Kotlin, Swing, VFS).

---

## Goals

- [ ] Ship a stable v1 on the JetBrains Marketplace
- [ ] Support the full DDD lifecycle: init → steps → tasks → done → new context
- [ ] Be agent-agnostic: works with JetBrains AI Assistant, any AI tool
- [ ] Keep zero external dependencies — pure Markdown + Git

## Out of scope

- No external server or cloud service
- No AI model bundled — the plugin structures context, not intelligence

