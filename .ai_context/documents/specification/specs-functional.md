# DDD Plugin — JetBrains — Functional Specifications

## Purpose

Give any JetBrains IDE user a native DDD experience — without leaving the IDE,
without copy-pasting context, and without any external tooling.

---

## User Stories

### US-01 — Project detection
> As a developer, when I open a project, I want the plugin to detect if DDD is already set up,
> so that I know whether I need to initialize it.

**Acceptance criteria:**
- [ ] If `ai_md_files/` exists → show a green "DDD Ready ✅" balloon notification
- [ ] If `ai_md_files/` does not exist → show a balloon: "No DDD context found. Initialize? [Yes] [Later]"
- [ ] Clicking "Yes" triggers the initialization wizard

---

### US-02 — Project initialization
> As a developer, when I initialize a DDD project, I want the plugin to scaffold the full `ai_md_files/`
> structure with templates adapted to my tech stack, so that I can start immediately.

**Acceptance criteria:**
- [ ] The plugin detects the stack (Angular, Spring Boot, Python, Rust, Go, Generic)
- [ ] It creates `ai_md_files/best-practices.md` pre-filled for the detected stack
- [ ] It creates `README_AI.md` at project root with the AI agent interaction contract (prohibitions, permissions, checklist, reference links) — only if not already present
- [ ] It creates the `docs/`, `features/`, `migrations/` folders with `.gitkeep`
- [ ] It opens `best-practices.md` automatically after init
- [ ] A notification confirms: "DDD project initialized for [stack] ✅"

---

### US-03 — New Feature Context
> As a developer, I want to create a new feature context folder in one click,
> so that I don't have to manually create files and copy templates.

**Acceptance criteria:**
- [ ] Right-click on any folder → DDD → "New Feature Context"
- [ ] A dialog asks for the feature name (e.g. "authentication")
- [ ] The plugin creates `ai_md_files/features/authentication/`:
  - `specs-functional.md` (pre-filled template)
  - `specs-technical.md` (pre-filled template)
  - `DONE.md` (empty, with header)
- [ ] The `specs-functional.md` is opened automatically

---

### US-04 — New Migration Plan
> As a developer, I want to create a new migration plan folder in one click,
> so that I can immediately start documenting before executing.

**Acceptance criteria:**
- [ ] Right-click on any folder → DDD → "New Migration Plan"
- [ ] A dialog asks for the migration name (e.g. "angular-21")
- [ ] The plugin creates `ai_md_files/migrations/angular-21/`:
  - `migration-plan.md` (pre-filled checklist template)
  - `MIGRATION_DONE.md` (empty, with header)
- [ ] The `migration-plan.md` is opened automatically

---

### US-05 — Tool Window Panel
> As a developer, I want a dedicated DDD panel in the IDE sidebar,
> so that I can navigate all DDD files at a glance without using the file explorer.

**Acceptance criteria:**
- [ ] A "DDD" panel appears in the right sidebar
- [ ] It shows a tree of `ai_md_files/` with icons per file type
- [ ] Clicking a file opens it in the editor
- [ ] Action buttons at the top: `+ Feature`, `+ Migration`, `Open best-practices.md`
- [ ] A badge shows "DDD Ready ✅" or "Not initialized ⚠️"

---

### US-06 — View DONE.md
> As a developer, I want to quickly open the DONE.md relevant to my current file,
> so that I can review what the AI did without navigating the tree.

**Acceptance criteria:**
- [ ] Action available via right-click and tool window
- [ ] Finds the nearest `DONE.md` or `MIGRATION_DONE.md` relative to the current file
- [ ] Opens it in a split editor

---

### US-07 — Export Copilot Chat to DDD Markdown
> As a developer, I want to export my Copilot conversation as a versioned Markdown file
> into `ai_md_files/docs/`, so that important AI decisions are preserved and committed.

**Background :**
The Copilot plugin stores conversations in a Nitrite/MVStore database under :
- Windows : `%APPDATA%\github-copilot\` or `%USERPROFILE%\.config\github-copilot\`
- Mac/Linux : `~/.config/github-copilot/`

⚠️ The database file is **locked** while IntelliJ is open. Direct export is not possible from within a running session.

**Acceptance criteria:**
- [ ] Action available via DDD menu: "DDD: Export Copilot Chat"
- [ ] A dialog asks for the session title (e.g. "DDD-session", "migration-angular-21")
- [ ] The plugin detects the Copilot database path automatically (Windows + Mac/Linux)
- [ ] It generates a Markdown guide file named `YYYY-MM-DD_<title>.md` in `ai_md_files/docs/`
- [ ] The guide explains the 3 export options :
  1. Export via dedicated Java project (gist nineninesevenfour)
  2. Manual copy-paste from the Copilot panel
  3. PowerShell / shell commands for Windows
- [ ] The file is opened automatically after creation
- [ ] A notification confirms: "DDD — Export Guide Created ✅"

---

### US-08 — Auto-refresh DDD Tool Window
> As a developer, I want the DDD tree view to refresh automatically when I add, delete
> or rename a `.md` file in `ai_md_files/`, without having to manually reload the panel.

**Acceptance criteria:**
- [ ] A VFS listener (`DddFileWatcher`) is registered for each project on open
- [ ] Any `.md` create / delete / rename / move event inside `ai_md_files/` triggers a refresh
- [ ] The tree model is rebuilt and all top-level nodes are re-expanded
- [ ] The status label (DDD Ready / Not initialized) is also updated
- [ ] The listener is disconnected cleanly when the project is closed
- [ ] Performance: no refresh triggered for files outside `ai_md_files/`

---

## Out of Scope for v1.0

- AI context injection (planned v1.1 — depends on JetBrains AI API stability)
- DONE.md diff viewer
- Multi-project / monorepo support
- Cloud sync of DDD templates
- Native Nitrite database reading from within IntelliJ (requires closing the project)

