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
        # `.ai_context` — Documentation-Driven Development v2

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

        ## How to work with this folder

        ### 1. Read before acting
        Before starting a work session, the agent **must read**:
        - `CONTRACT.md` → the permanent rules of the project and the developer
        - `CONTEXT.md` → the objective and tasks of the current context
        - `documents/specification/` → functional and architectural details
        - `documents/technical/` → technical decisions and best practices

        ### 2. Work by adding contextual documents
        - A new technical constraint → a file in `documents/technical/`
        - A clarified functional need → a file in `documents/specification/`
        - A completed task → a summary file in `documents/done/`

        ### `permanent-` Convention
        A file in `technical/` or `specification/` prefixed with `permanent-` **will not be deleted**
        when switching to a new context.

        ### 3. Close a context before committing
        A **context = a unit of work = a Git commit**.
        Commit `.ai_context/` before moving to a new context.

        ---

        ## Structure

        ```
        .ai_context/
        ├── README.md              ← this file (permanent)
        ├── CONTRACT.md            ← rules for the agent (permanent)
        ├── CONTEXT.md             ← objective and todo list (contextual)
        ├── context.json           ← machine metadata (contextual)
        ├── history.log            ← past contexts journal (permanent)
        ├── skills/                ← skills and competencies (permanent-* kept)
        └── documents/
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

            ## 📁 Reference documentation

            All context is centralized in [`.ai_context/`](./.ai_context/).
            **Read and follow the directives in these files** before any modification.
        """.trimIndent()
  }

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

  fun contextJson(title: String, description: String): String {
    val escaped = { s: String -> s.replace("\"", "\\\"") }
    return """{"title":"${escaped(title)}","description":"${escaped(description)}","startedAt":"${nowIso()}"}"""
  }

  fun projectReadme(projectName: String): String = """
        # For AI Agent :

        Read all [context](./.ai_context) for context and needs.
        The agent must apply the conditions specified in CONTRACT.md.
        The current development context is presented in CONTEXT.md and all files in the `documents/` directory.
        The `done/` subdirectory contains MD files written by the agent explaining what was done in this context.
        The `technical/` subdirectory contains best practices and technical guidelines.
        The `specification/` subdirectory contains functional and architectural details.

        ---

        # $projectName

        > *(Describe your project here)*
    """.trimIndent()

  // ── Scaffold helpers ───────────────────────────────────────────────────────

  fun scaffoldInit(
    aiContextRoot: File,
    profile: AgentProfile,
    title: String,
    description: String,
    todos: List<String>
   ) {
     aiContextRoot.mkdirs()
     for (sub in listOf("done", "specification", "technical")) {
       File(aiContextRoot, "documents/$sub").mkdirs()
       File(aiContextRoot, "documents/$sub/.gitkeep").also {
         if (!it.exists()) it.writeText("")
       }
     }
     File(aiContextRoot, "skills").mkdirs()
     File(aiContextRoot, "skills/.gitkeep").also {
       if (!it.exists()) it.writeText("")
     }
     File(aiContextRoot, "README.md").also { if (!it.exists()) it.writeText(aiContextReadme()) }
    File(aiContextRoot, "CONTRACT.md").also { if (!it.exists()) it.writeText(contractMd(profile)) }
    File(aiContextRoot, "CONTEXT.md").writeText(contextMd(title, description, todos))
    File(aiContextRoot, "context.json").writeText(contextJson(title, description))

    // Create or update project README.md at root
    val projectRoot = aiContextRoot.parentFile
    val projectReadme = File(projectRoot, "README.md")
    if (!projectReadme.exists()) {
      projectReadme.writeText(projectReadme(projectRoot.name))
    } else if (!projectReadme.readText().contains(".ai_context")) {
      val agentHeader = """
                # For AI Agent :

                Read all [context](./.ai_context) for context and needs.
                The agent must apply the conditions specified in CONTRACT.md.
                The current development context is presented in CONTEXT.md and all files in the `documents/` directory.

                ---

            """.trimIndent()
      projectReadme.writeText(agentHeader + "\n" + projectReadme.readText())
    }
  }

  fun scaffoldNewContext(
    aiContextRoot: File,
    title: String,
    description: String,
    todos: List<String>
  ) {
    // Archive old context into history.log
    val contextJsonFile = File(aiContextRoot, "context.json")
    if (contextJsonFile.exists()) {
      val oldJson = contextJsonFile.readText().trim().trimEnd('}')
      val historyLine = """$oldJson,"endedAt":"${nowIso()}"}"""
      File(aiContextRoot, "history.log").appendText(historyLine + "\n")
    }

     // Clear done/ entirely
     File(aiContextRoot, "documents/done").listFiles()?.forEach { it.delete() }

     // Clear specification/, technical/, and skills/ except permanent-*
     for (sub in listOf("specification", "technical", "skills")) {
       val dir = if (sub == "skills") File(aiContextRoot, sub) else File(aiContextRoot, "documents/$sub")
       dir.listFiles()
         ?.filter { !it.name.startsWith("permanent-") && it.name != ".gitkeep" }
         ?.forEach { it.delete() }
     }

    // Write new contextual files
    File(aiContextRoot, "CONTEXT.md").writeText(contextMd(title, description, todos))
    File(aiContextRoot, "context.json").writeText(contextJson(title, description))
  }
}
