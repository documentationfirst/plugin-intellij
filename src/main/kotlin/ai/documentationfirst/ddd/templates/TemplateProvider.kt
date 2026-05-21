package ai.documentationfirst.ddd.templates

import java.io.File
import java.time.Instant
import java.time.LocalDate

enum class AgentProfile(val label: String, val description: String) {
  STRICT(
    "Strict (Recommended)",
    "No terminal commands. Agent displays commands as code blocks only. No rename/delete."
  ),
  STANDARD(
    "Standard",
    "Build/install forbidden. Read-only commands (grep, find, cat) and tests allowed."
  ),
  PERMISSIVE(
    "Permissive",
    "All terminal commands allowed. Agent may rename/delete with caution."
  ),
}

object TemplateProvider {

  private fun today() = LocalDate.now().toString()
  private fun nowIso() = Instant.now().toString()

  // ── Static templates (permanent files) ────────────────────────────────────

  fun aiContextReadme(): String = """
        # `.ai_context` — Documentation-Driven Development

        This folder is managed by the **Documentation First** plugin (JetBrains).
        It structures collaboration between the developer and the AI agent throughout the project.

        ---

        ## Philosophy

        The method is based on a simple principle: **documentation is not a deliverable, it is a working tool**.

        The developer and the AI agent collaborate by **reading and writing documents**.
        - The developer writes what they want to do, understand or decide.
        - The agent reads, completes, refines, questions, and documents what it has done.
        - Together, they maintain a living documentation base that serves as the project's memory.

        > It is not the agent who decides — it is the developer who drives through documents.

        ---

        ## Reading order (before any action)

        1. `README.md` — this file
        2. `CONTEXT.md` — current sprint/task focus
        3. `CONTRACT.md` — interaction rules
        4. `vision.md` — product vision and epic goals
        5. `skills/` — permanent agent behaviours
        6. `steps/` — roadmap phases and features
        7. `tasks/specification/` — active specs
        8. `tasks/done/` — what was already implemented
        9. `tasks/technical/` — permanent technical references

        ---

        ## `permanent-` Convention
        A file prefixed with `permanent-` **will not be deleted** when switching to a new context.
        Applies to `tasks/specification/`, `tasks/technical/`, and `skills/`.

        ### Close a context before committing
        A **context = a unit of work = a Git commit**.
        Commit `.ai_context/` before moving to a new context.

        ---

        ## Structure

        ```
        .ai_context/
        ├── README.md              ← this file (permanent)
        ├── CONTRACT.md            ← rules for the agent (permanent)
        ├── CONTEXT.md             ← current context: objective and todo list (contextual)
        ├── context.json           ← machine metadata (contextual)
        ├── vision.md              ← product vision and epic goals (permanent)
        ├── history.json           ← past contexts journal in JSON Lines (permanent)
        ├── skills/                ← permanent agent behaviours (permanent-* kept)
        ├── steps/                 ← roadmap phases / features (permanent)
        └── tasks/
            ├── done/              ← agent summaries (contextual)
            ├── specification/     ← functional specs (permanent-* kept)
            └── technical/         ← technical decisions (permanent-* kept)
        ```

        ---

        *Managed by [Documentation First Plugin](https://documentationfirst.ai) — MIT License*
    """.trimIndent()

  fun contractMd(profile: AgentProfile): String {
    val prohibitions = when (profile) {
      AgentProfile.STRICT -> """
                ## 🚫 ABSOLUTE PROHIBITIONS

                > These rules apply **without exception**.

                1. **Never use any terminal command execution tool.**
                   - Forbidden: `npm install`, `npm run`, `ng build`, `git`, `node`, etc.
                   - If a command is needed, display it as a code block — the developer runs it themselves.
                2. **Never rename or delete files.**
                3. **Never modify files outside the workspace.**
            """.trimIndent()

      AgentProfile.STANDARD -> """
                ## ⚠️ RESTRICTIONS

                1. **Never run build, install or serve commands.**
                   - Forbidden: `npm install`, `npm run build`, `ng build`, `ng serve`, `git push`, etc.
                   - Allowed read-only: `grep`, `find`, `cat`, `ls`
                   - Test commands (`ng test`, `npm test`) are allowed.
                2. **Never rename or delete files.**
                3. **Never modify files outside the workspace.**
            """.trimIndent()

      AgentProfile.PERMISSIVE -> """
                ## ℹ️ RECOMMENDATIONS

                1. **Never modify files outside the workspace.**
                2. **Display destructive commands** (delete, rename, publish) as a code block for review.
                3. Build, install, test and serve commands may be executed directly if needed.
            """.trimIndent()
    }

    val permissions = when (profile) {
      AgentProfile.STRICT -> """
                ## ✅ What the agent is allowed to do

                | Action | Allowed | Notes |
                |---|---|---|
                | Modify existing files | ✅ Yes | Without prior confirmation |
                | Create new technical files | ✅ Yes | |
                | Read project files | ✅ Yes | |
                | Search through code | ✅ Yes | Read-only |
                | Update `.ai_context/` | ✅ Yes | |
                | Execute terminal commands | ❌ No | Display as code block only |
                | Rename or delete files | ❌ No | |
            """.trimIndent()

      AgentProfile.STANDARD -> """
                ## ✅ What the agent is allowed to do

                | Action | Allowed | Notes |
                |---|---|---|
                | Modify existing files | ✅ Yes | |
                | Create new files | ✅ Yes | |
                | Read project files | ✅ Yes | |
                | Read-only commands (`grep`, `find`, `cat`) | ✅ Yes | |
                | Run tests (`npm test`, `ng test`) | ✅ Yes | |
                | Build / install / serve commands | ❌ No | Display as code block |
                | Rename or delete files | ❌ No | |
            """.trimIndent()

      AgentProfile.PERMISSIVE -> """
                ## ✅ What the agent is allowed to do

                | Action | Allowed | Notes |
                |---|---|---|
                | Modify / create files | ✅ Yes | |
                | Read files | ✅ Yes | |
                | All terminal commands | ✅ Yes | |
                | Rename / delete | ✅ Yes | With care |
                | Modify files outside workspace | ❌ No | |
            """.trimIndent()
    }

    return """
            # AI Agent — Interaction Contract

            *Profile: **${profile.name.lowercase()}***

            This file defines the rules of interaction between the developer and the AI agent on this project.
            **The agent must read and respect this contract before taking any action.**

            ---

            $prohibitions

            ---

            $permissions

            ---

            ## 🧠 Communication preferences

            - Reply in **English**
            - Be **concise**: do not repeat existing code in explanations
            - Report **pre-existing errors** separately from errors introduced by modifications
            - Do not ask for confirmation on obvious changes — **act directly**
            - When in doubt about scope, **ask one focused question**

            ---

            ## 🔄 Session close protocol

            At the end of a session, update `dev-context.json` → `lastSession` with:
            ```json
            "lastSession": {
              "date": "YYYY-MM-DD",
              "done": "one sentence — what was implemented",
              "remaining": "what is left in the current task",
              "blocker": "what blocked or is unclear (empty if none)"
            }
            ```

            **Trigger this automatically when:**
            1. The developer signals end of session ("stop", "commit", "done", "à demain", etc.)
            2. More than 5 files were modified in this session
            3. A significant feature or bug fix was just completed
            4. A new major topic is about to begin — natural breakpoint before context switch

            This is a lightweight handoff — not a full `done.md`. One sentence per field.

            ---

            ## 📁 Reference documentation

            All context is centralized in [`.ai_context/`](./.ai_context/).
            **Read and follow the directives in these files** before any modification.
        """.trimIndent()
  }

  // ── Permanent templates ────────────────────────────────────────────────────

  fun visionMd(vision: String): String = """
        # Vision

        *Created: ${today()}*

        ---

        ## Epic

        $vision

        ---

        ## Goals

        - [ ] ...

        ## Out of scope

        - ...
    """.trimIndent()

  fun stepMd(name: String, description: String): String = """
        # Step — $name

        *Created: ${today()}*

        ---

        ## Objective

        ${if (description.isBlank()) "*(describe this step)*" else description}

        ---

        ## Tasks

        - [ ] ...
    """.trimIndent()

  // ── Contextual templates ───────────────────────────────────────────────────

  fun contextMd(title: String, description: String, todos: List<String>): String {
    val todoLines = if (todos.isEmpty()) "- [ ] ..." else todos.joinToString("\n") { "- [ ] $it" }
    return """
            # Context — $title

            *Started: ${today()}*

            ---

            ## Description

            $description

            ---

            ## Todo

            $todoLines
        """.trimIndent()
  }

  fun devContextJson(
    title: String,
    description: String,
    vision: String,
    steps: List<Triple<String, String, Boolean>>,  // name, description, done
    todos: List<String>
  ): String {
    val esc = { s: String -> s.replace("\\", "\\\\").replace("\"", "\\\"") }
    val stepsJson = steps.joinToString(",\n    ") { (name, desc, done) ->
      """{"name":"${esc(name)}","description":"${esc(desc)}","done":$done}"""
    }
    val todosJson = todos.joinToString(",\n      ") { t ->
      """{"text":"${esc(t)}","done":false}"""
    }
    return """{
  "vision": "${esc(vision)}",
  "steps": [
    $stepsJson
  ],
  "task": {
    "title": "${esc(title)}",
    "description": "${esc(description)}",
    "startedAt": "${nowIso()}",
    "todos": [
      $todosJson
    ]
  }
}"""
  }

  fun methodologySkillMd(): String = """
        # Skill — DDD Methodology (permanent)

        *Created: ${today()}*

        > This file is permanent — it survives every context and vision switch.
        > It defines how the agent must work on this project at all times.

        ---

        ## Reading order (before any action)

        ### 🔒 Fixed reference — read once, always valid
        1. `CONTEXT.md` — who we are, what we build, the stack, team conventions *(permanent)*
        2. `CONTRACT.md` — agent interaction rules *(permanent)*

        ### 🔄 Current development work — read every session
        3. `vision.md` — product direction and epic goals
        4. `steps/` — roadmap phases for the current vision
        5. `dev-context.json` — active task: title, description, todos, step progress
        6. `tasks/specification/` — functional specs for the current task
        7. `tasks/done/` — what was already implemented
        8. `tasks/technical/` — technical decisions

        ---

        ## Session protocol

        ### 1. Read before acting
        Read all context files above in order before taking any action.
        > These files are the project's memory. Never assume the state of the code without reading them.

        ### 2. Spec before code
        For any new feature, fix, or refactoring:
        1. Create `tasks/specification/spec-<feature>.md` with: context, expected behaviour, components, plan
        2. Optionally generate `tasks/specification/spec-<feature>-preview.html` for UI tasks
        3. Wait for explicit developer validation before writing any production code

        ### 3. Implement
        - Follow the validated spec to the letter
        - Group changes by file
        - Validate errors after each file modified

        ### 4. Write the done report
        After implementation, create `tasks/done/done-<feature>.md`:
        - Summary of changes
        - Per item: problem → root cause (if bug) → solution → modified files
        - Optionally generate `tasks/done/done-<feature>-test.html` for acceptance verification

        ---

        ## Artefact conventions

        | File | Location | Written by | Purpose |
        |---|---|---|---|
        | `spec-*.md` | `tasks/specification/` | Human | Intent |
        | `spec-*-preview.html` | `tasks/specification/` | AI | Visual validation before coding |
        | `done-*.md` | `tasks/done/` | AI | Execution report |
        | `done-*-test.html` | `tasks/done/` | AI | Acceptance test runner |

        Prefix any file with `permanent-` to preserve it across context resets.

        ---

        ## Communication rules

        - If the request is ambiguous: ask ONE focused question, wait for the answer
        - If the developer explains something: update mental context only — do not act unless explicitly asked
        - If a bug is found in passing: signal it, do not fix without agreement (unless it blocks the current task)
        - A developer explanation is not an action order
    """.trimIndent()

  fun contextProjectMd(projectContext: String): String = """
        # Project Context

        *Created: ${today()}*

        ---

        ## What we are building

        $projectContext

        ---

        ## Stack & conventions

        *(describe the stack, language, framework, key rules)*

        ---

        ## Team rules

        *(describe how the agent should work: language, code style, what it must never do)*
    """.trimIndent()

  fun newTaskSpecMd(title: String, description: String): String {
    return """
        # Spec — $title

        *Created: ${today()}*

        ---

        ## Objective

        ${if (description.isBlank()) "*(describe the objective of this task)*" else description}

        ---

        ## Expected behaviour

        *(describe the expected behaviour)*

        ---

        ## Plan

        *(describe the implementation plan)*

        ---

        > 💡 **Next step**: ask your AI agent to read `.ai_context/` and implement this spec.
    """.trimIndent()
  }

  private fun newTaskSpecFilename(title: String): String =
    "spec-" + title.lowercase().replace(Regex("\\s+"), "-").replace(Regex("[^a-z0-9-]"), "") + ".md"
    val escaped = { s: String -> s.replace("\"", "\\\"") }
    return """{"title":"${escaped(title)}","description":"${escaped(description)}","startedAt":"${nowIso()}"}"""
  }

  private fun historyLine(type: String, title: String, completedStep: String = "", vision: String = ""): String {
    val esc = { s: String -> s.replace("\"", "\\\"") }
    val visionPart = if (vision.isNotBlank()) ""","vision":"${esc(vision)}"""" else ""
    val stepPart = if (completedStep.isNotBlank()) ""","completedStep":"${esc(completedStep)}"""" else ""
    return """{"type":"$type","title":"${esc(title)}"$visionPart$stepPart,"endedAt":"${nowIso()}"}"""
  }

  fun projectReadmeMd(projectName: String): String = """
        # For AI Agent

        > Read `.ai_context/` before any action.

        - **WHO/WHAT**: `CONTEXT.md` — project identity, stack, conventions *(permanent reference, like a README)*
        - **HOW**: `CONTRACT.md` — interaction rules *(permanent)*
        - **WHERE WE'RE GOING**: `vision.md` + `steps/` — product direction and roadmap phases
        - **WHAT WE'RE DOING NOW**: `dev-context.json` — active task title, description, todos and step progress
        - **TASK DETAILS**: `tasks/specification/`, `tasks/done/`, `tasks/technical/`

        ---

        # $projectName

        > *(Describe your project here)*
    """.trimIndent()

  // ── Scaffold helpers ───────────────────────────────────────────────────────

  fun scaffoldInit(
    aiContextRoot: File,
    profile: AgentProfile,
    projectContext: String,
    vision: String,
    title: String,
    description: String,
    todos: List<String>,
    steps: List<Pair<String, String>>
   ) {
     aiContextRoot.mkdirs()
     for (sub in listOf("done", "specification", "technical")) {
       File(aiContextRoot, "tasks/$sub").mkdirs()
       File(aiContextRoot, "tasks/$sub/.gitkeep").also { if (!it.exists()) it.writeText("") }
     }
     File(aiContextRoot, "skills").mkdirs()
     File(aiContextRoot, "skills/.gitkeep").also { if (!it.exists()) it.writeText("") }
     // Default methodology skill (if not already present)
     val methodologyFile = File(aiContextRoot, "skills/permanent-methodology.md")
     if (!methodologyFile.exists()) { methodologyFile.writeText(methodologySkillMd()) }
     File(aiContextRoot, "steps").mkdirs()
     File(aiContextRoot, "steps/.gitkeep").also { if (!it.exists()) it.writeText("") }
     for ((name, desc) in steps) {
       val slug = name.lowercase().replace(Regex("\\s+"), "-").replace(Regex("[^a-z0-9-]"), "")
       File(aiContextRoot, "steps/$slug.md").writeText(stepMd(name, desc))
     }
     File(aiContextRoot, "README.md").also { if (!it.exists()) it.writeText(aiContextReadme()) }
     File(aiContextRoot, "CONTRACT.md").also { if (!it.exists()) it.writeText(contractMd(profile)) }
     File(aiContextRoot, "CONTEXT.md").also { if (!it.exists()) it.writeText(contextProjectMd(projectContext)) }
     File(aiContextRoot, "vision.md").also { if (!it.exists()) it.writeText(visionMd(vision)) }

     val stepsTriple = steps.map { (name, desc) -> Triple(name, desc, false) }
     File(aiContextRoot, "dev-context.json").writeText(devContextJson(title, description, vision, stepsTriple, todos))

     // Create spec file for the first task
     File(aiContextRoot, "tasks/specification/${newTaskSpecFilename(title)}").writeText(newTaskSpecMd(title, description))

    val projectRoot = aiContextRoot.parentFile
    val projectReadme = File(projectRoot, "README.md")
    if (!projectReadme.exists()) {
      projectReadme.writeText(projectReadmeMd(projectRoot.name))
    } else if (!projectReadme.readText().contains(".ai_context")) {
      val agentHeader = """
                # For AI Agent

                > Read `.ai_context/` before any action.

                - **WHO/WHAT**: `CONTEXT.md` — project identity, stack, conventions *(permanent)*
                - **WHERE WE'RE GOING**: `vision.md` + `steps/` — product direction and roadmap phases
                - **WHAT WE'RE DOING NOW**: `dev-context.json` — active task, todos and step progress

                ---

            """.trimIndent()
      projectReadme.writeText(agentHeader + "\n" + projectReadme.readText())
    }
  }

  fun scaffoldNewVision(
    aiContextRoot: File,
    vision: String,
    steps: List<Pair<String, String>>,
    title: String,
    description: String,
    todos: List<String>
  ) {
    // Archive to history.json
    val devContextFile = File(aiContextRoot, "dev-context.json")
    if (devContextFile.exists()) {
      val oldTitle = Regex(""""title"\s*:\s*"([^"]*?)"""").find(devContextFile.readText())?.groupValues?.get(1) ?: ""
      File(aiContextRoot, "history.json").appendText(historyLine("vision", oldTitle) + "\n")
    }

    // Reset steps/ entirely
    File(aiContextRoot, "steps").also { dir ->
      dir.mkdirs()
      dir.listFiles()?.filter { it.name != ".gitkeep" }?.forEach { it.delete() }
    }
    for ((name, desc) in steps) {
      val slug = name.lowercase().replace(Regex("\\s+"), "-").replace(Regex("[^a-z0-9-]"), "")
      File(aiContextRoot, "steps/$slug.md").writeText(stepMd(name, desc))
    }

    // Reset tasks/ non-permanent
    clearTasks(aiContextRoot)

    // Overwrite vision.md and dev-context.json
    File(aiContextRoot, "vision.md").writeText(visionMd(vision))
    val stepsTriple = steps.map { (name, desc) -> Triple(name, desc, false) }
    devContextFile.writeText(devContextJson(title, description, vision, stepsTriple, todos))

    // Create spec file for the first task of the new vision
    File(aiContextRoot, "tasks/specification/${newTaskSpecFilename(title)}").writeText(newTaskSpecMd(title, description))
  }

  fun scaffoldNewTask(
    aiContextRoot: File,
    completedStep: String,
    title: String,
    description: String,
    todos: List<String>,
    specsToDelete: List<String> = emptyList()
  ) {
    val devContextFile = File(aiContextRoot, "dev-context.json")
    var currentVision = ""
    var currentSteps = listOf<Triple<String, String, Boolean>>()

    if (devContextFile.exists()) {
      val text = devContextFile.readText()
      // Extract vision
      currentVision = Regex(""""vision"\s*:\s*"([^"]*?)"""").find(text)?.groupValues?.get(1) ?: ""
      val oldTaskTitle = Regex(""""title"\s*:\s*"([^"]*?)"""").find(text)?.groupValues?.get(1) ?: ""
      // Parse steps — mark completedStep as done
      val stepRegex = Regex(""""name"\s*:\s*"([^"]*?)"[^}]*?"description"\s*:\s*"([^"]*?)"[^}]*?"done"\s*:\s*(true|false)""")
      currentSteps = stepRegex.findAll(text).map { m ->
        val name = m.groupValues[1]
        val desc = m.groupValues[2]
        val wasDone = m.groupValues[3] == "true"
        Triple(name, desc, wasDone || (completedStep.isNotBlank() && name.lowercase().contains(completedStep.lowercase())))
      }.toList()
      File(aiContextRoot, "history.json").appendText(
        historyLine("task", oldTaskTitle, completedStep, currentVision) + "\n"
      )
    }

    clearTasks(aiContextRoot, specsToDelete)

    // Append retrospective section to the completed step file
    if (completedStep.isNotBlank()) {
      val slug = completedStep.lowercase().replace(Regex("\\s+"), "-").replace(Regex("[^a-z0-9-]"), "")
      val stepFile = File(aiContextRoot, "steps/$slug.md")
      if (stepFile.exists()) {
        stepFile.appendText("\n\n## Retrospective — ${today()}\n\n- ✅ What worked:\n- ⚠️ What blocked:\n- 📌 To remember:\n")
      }
    }
    devContextFile.writeText(devContextJson(title, description, currentVision, currentSteps, todos))

    // Create spec file for the new task
    File(aiContextRoot, "tasks/specification/${newTaskSpecFilename(title)}").writeText(newTaskSpecMd(title, description))
  }

  private fun clearTasks(aiContextRoot: File, specsToDelete: List<String> = emptyList()) {
    // done/ — always cleared entirely
    File(aiContextRoot, "tasks/done").listFiles()
      ?.filter { !it.name.startsWith("permanent-") && it.name != ".gitkeep" }
      ?.forEach { it.delete() }

    // specification/ — only delete explicitly selected files
    File(aiContextRoot, "tasks/specification").listFiles()
      ?.filter { !it.name.startsWith("permanent-") && it.name != ".gitkeep" && specsToDelete.contains(it.name) }
      ?.forEach { it.delete() }

    // technical/ — clear non-permanent
    File(aiContextRoot, "tasks/technical").listFiles()
      ?.filter { !it.name.startsWith("permanent-") && it.name != ".gitkeep" }
      ?.forEach { it.delete() }
  }
}
