# DDD Plugin — JetBrains — Technical Specifications

## Overview

A JetBrains IDE plugin that makes Documentation-Driven Development v2 (DDD2) a first-class AI citizen
of the IntelliJ Platform (IntelliJ IDEA, WebStorm, Rider, PyCharm, GoLand…).

**Target Marketplace:** [plugins.jetbrains.com](https://plugins.jetbrains.com)
**Language:** Kotlin
**Build system:** Gradle + `org.jetbrains.intellij` plugin
**Min platform version:** 2024.1 (IC-241)

---

## Project Structure

```
ddd-plugin-jetbrains/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── ai/documentationfirst/ddd/
│       │       ├── DddPlugin.kt                  ← Plugin entry point
│       │       ├── startup/
│       │       │   └── DddProjectStartupActivity.kt  ← Detects ai_md_files/ on open
│       │       ├── toolwindow/
│       │       │   ├── DddToolWindowFactory.kt    ← Side panel factory
│       │       │   └── DddToolWindowPanel.kt      ← Panel UI (tree + actions)
│       │       ├── actions/
│       │       │   ├── NewFeatureContextAction.kt ← "DDD: New Feature Context"
│       │       │   ├── NewMigrationPlanAction.kt  ← "DDD: New Migration Plan"
│       │       │   └── ViewDoneAction.kt          ← "DDD: View DONE.md"
│       │       ├── templates/
│       │       │   └── TemplateProvider.kt        ← MD templates by language/framework
│       │       ├── context/
│       │       │   └── AiContextInjector.kt       ← Injects .md into JetBrains AI context
│       │       └── notifications/
│       │           └── DddNotifications.kt        ← Balloon notifications
│       └── resources/
│           ├── META-INF/
│           │   └── plugin.xml                    ← Plugin descriptor
│           └── templates/                        ← .md template files per stack
│               ├── angular/
│               │   ├── best-practices.md
│               │   ├── specs-functional.md
│               │   └── specs-technical.md
│               ├── spring-boot/
│               │   ├── best-practices.md
│               │   ├── specs-functional.md
│               │   └── specs-technical.md
│               ├── python/
│               └── rust/
└── src/test/kotlin/...
```

---

## Core Features

### 1. Project Startup Detection

**File:** `DddProjectStartupActivity.kt`
**Trigger:** `ProjectActivity` — runs once when a project is opened.

```kotlin
class DddProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val basePath = project.basePath ?: return
        val aiMdFiles = File("$basePath/ai_md_files")

        if (!aiMdFiles.exists()) {
            // Show balloon: "No ai_md_files/ detected. Initialize DDD project?"
            DddNotifications.showInitPrompt(project)
        } else {
            // Show balloon: "DDD project detected ✅"
            DddNotifications.showDddReady(project)
        }
    }
}
```

---

### 2. Tool Window (Side Panel)

**File:** `DddToolWindowFactory.kt`
**Registration:** `plugin.xml` → `<toolWindow id="DDD" .../>`

The panel displays:
- A **tree view** of `ai_md_files/` with icons per file type
- Action buttons: `+ New Feature`, `+ New Migration`, `Open best-practices.md`
- A **"DDD Ready" badge** (green ✅ or grey ⚠️ depending on folder presence)

File type icons:
| File | Icon |
|------|------|
| `best-practices.md` | ⚙️ |
| `specs-functional.md` | 📋 |
| `specs-technical.md` | 🔧 |
| `DONE.md` / `MIGRATION_DONE.md` | ✅ |
| `migration-*.md` | 🗺️ |
| `docs/*.md` | 📚 |

---

### 3. Actions (Right-click + Tool Window)

Registered in `plugin.xml` under `<actions>`. Available via:
- Right-click in Project tree → **DDD** submenu
- Tool Window buttons
- `Help > Find Action` (Ctrl+Shift+A)

| Action ID | Label | Description |
|-----------|-------|-------------|
| `ddd.newFeatureContext` | DDD: New Feature Context | Scaffolds `features/{name}/specs-functional.md`, `specs-technical.md`, `DONE.md` |
| `ddd.newMigrationPlan` | DDD: New Migration Plan | Scaffolds `migrations/{name}/migration-plan.md`, `MIGRATION_DONE.md` |
| `ddd.viewDone` | DDD: View DONE.md | Opens the nearest `DONE.md` relative to the current file |
| `ddd.initProject` | DDD: Initialize Project | Creates the full `ai_md_files/` structure with templates for the detected stack |

---

### 4. Template Provider

**File:** `TemplateProvider.kt`

Detects the project stack by inspecting:
- `package.json` → Angular, React, Vue, Node
- `pom.xml` / `build.gradle.kts` → Spring Boot, Kotlin
- `requirements.txt` / `pyproject.toml` → Python / Django / FastAPI
- `Cargo.toml` → Rust
- `go.mod` → Go

Then copies the matching templates from `resources/templates/{stack}/` into `ai_md_files/`.

```kotlin
object TemplateProvider {
    fun detect(projectPath: String): Stack {
        return when {
            File("$projectPath/package.json").exists() -> detectNpmStack(projectPath)
            File("$projectPath/pom.xml").exists() -> Stack.SPRING_BOOT
            File("$projectPath/Cargo.toml").exists() -> Stack.RUST
            File("$projectPath/go.mod").exists() -> Stack.GO
            else -> Stack.GENERIC
        }
    }

    fun scaffold(project: Project, stack: Stack, featureName: String) {
        // Copy templates from resources/templates/{stack}/ to ai_md_files/features/{featureName}/
    }
}

enum class Stack { ANGULAR, REACT, SPRING_BOOT, PYTHON, RUST, GO, GENERIC }
```

---

### 5. AI Context Injection

**File:** `AiContextInjector.kt`

JetBrains AI (via the AI Assistant plugin) supports context injection through the
`com.intellij.ai.context` extension point (available since platform 2024.2, still evolving).

Strategy:
1. On every AI prompt, attach the content of `best-practices.md` as system context
2. If the current file is inside `ai_md_files/features/{name}/`, also attach the relevant `specs-technical.md`
3. Use `FileEditorManager.getInstance(project).selectedFiles` to detect current context

> ⚠️ **Note:** The JetBrains AI context API is still experimental as of 2024.
> The injection mechanism may need to use the clipboard or a custom prompt prefix as a fallback.

---

### 6. plugin.xml (descriptor)

```xml
<idea-plugin>
  <id>ai.documentationfirst.ddd</id>
  <name>DDD2 — Documentation-Driven Development v2</name>
  <version>1.0.0</version>
  <vendor url="https://documentationfirst.ai">Documentation First</vendor>
  <description>Make DDD a first-class AI citizen of your IDE.</description>

  <depends>com.intellij.modules.platform</depends>

  <extensions defaultExtensionNs="com.intellij">
    <toolWindow id="DDD" anchor="right"
                factoryClass="ai.documentationfirst.ddd.toolwindow.DddToolWindowFactory"
                icon="/icons/ddd.svg"/>
    <projectActivity
        implementation="ai.documentationfirst.ddd.startup.DddProjectStartupActivity"/>
  </extensions>

  <actions>
    <group id="DddActionGroup" text="DDD" popup="true">
      <add-to-group group-id="ProjectViewPopupMenu" anchor="last"/>
      <action id="ddd.newFeatureContext"
              class="ai.documentationfirst.ddd.actions.NewFeatureContextAction"
              text="DDD: New Feature Context"/>
      <action id="ddd.newMigrationPlan"
              class="ai.documentationfirst.ddd.actions.NewMigrationPlanAction"
              text="DDD: New Migration Plan"/>
      <action id="ddd.viewDone"
              class="ai.documentationfirst.ddd.actions.ViewDoneAction"
              text="DDD: View DONE.md"/>
    </group>
  </actions>
</idea-plugin>
```

---

## build.gradle.kts

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "ai.documentationfirst"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.1")
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }
    }
}
```

---

## Gradle Tasks

```bash
./gradlew runIde          # Launch a sandbox IDE with the plugin installed
./gradlew buildPlugin     # Build the .zip for distribution
./gradlew publishPlugin   # Publish to JetBrains Marketplace (requires token)
./gradlew verifyPlugin    # Run plugin verifier
```

---

## MD Templates (bundled)

Each stack ships with 3 pre-filled templates:

### `best-practices.md` (Angular example)
```markdown
# Best Practices — Angular

## Architecture
- Standalone components only (no NgModule)
- Signals for state management (no BehaviorSubject)
- OnPush change detection on all components

## Naming
- Components: PascalCase, suffix -Component
- Services: PascalCase, suffix -Service
- Files: kebab-case

## What to avoid
- Zone.js dependencies
- ngModel (use reactive forms)
- any type
```

### `specs-functional.md` (generic)
```markdown
# Functional Specifications — {feature}

## User Stories
- As a [role], I want [feature] so that [outcome].

## Acceptance Criteria
- [ ] ...

## Business Rules
- ...

## Edge Cases
- ...
```

### `specs-technical.md` (generic)
```markdown
# Technical Specifications — {feature}

## Architecture
- Component structure: ...
- State management: ...

## API Contract
- Endpoint: ...
- Request/Response: ...

## Dependencies
- Libraries: ...
- References: see ai_md_files/best-practices.md

## Known Constraints
- ...
```

---

## Testing Strategy

| Test type | Tool | Coverage target |
|-----------|------|----------------|
| Unit | JUnit 5 + MockK | Actions, TemplateProvider, ContextInjector |
| Integration | IntelliJ Platform test framework | StartupActivity, ToolWindow |
| UI | Not planned for v1.0 | — |

---

## Roadmap

| Version | Features |
|---------|----------|
| v1.0 | Detection, scaffolding, tool window, actions, templates (Angular, Spring Boot, Generic) |
| v1.1 | AI context injection (JetBrains AI Assistant) |
| v1.2 | More templates (Python, Rust, Go, React) |
| v2.0 | DONE.md diff viewer, AI session recap, DDD score per project |

