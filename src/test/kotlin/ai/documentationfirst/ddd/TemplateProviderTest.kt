package ai.documentationfirst.ddd

import ai.documentationfirst.ddd.templates.AgentProfile
import ai.documentationfirst.ddd.templates.TemplateProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class TemplateProviderTest {

    // Helper: default scaffoldInit call with all required params
    private fun defaultInit(root: File, title: String = "My Title", desc: String = "My Desc") {
        TemplateProvider.scaffoldInit(
            root, AgentProfile.STRICT,
            "My project context", "My vision",
            title, desc, emptyList(), emptyList()
        )
    }

    // ── scaffoldInit ──────────────────────────────────────────────────────────

    @Test
    fun `scaffoldInit creates the full directory structure`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root)

        assertTrue(root.exists())
        assertTrue(File(root, "README.md").exists())
        assertTrue(File(root, "CONTRACT.md").exists())
        assertTrue(File(root, "CONTEXT.md").exists())
        assertTrue(File(root, "context.json").exists())
        assertTrue(File(root, "vision.md").exists())
        assertTrue(File(root, "tasks/done").exists())
        assertTrue(File(root, "tasks/specification").exists())
        assertTrue(File(root, "tasks/technical").exists())
        assertTrue(File(root, "skills").exists())
        assertTrue(File(root, "steps").exists())
    }

    @Test
    fun `scaffoldInit writes projectContext in CONTEXT md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(
            root, AgentProfile.STRICT,
            "Refonte auth context", "Ma vision",
            "Titre tâche", "Objectif", emptyList(), emptyList()
        )

        val content = File(root, "CONTEXT.md").readText()
        assertTrue(content.contains("Refonte auth context"), "CONTEXT.md should contain projectContext")
    }

    @Test
    fun `scaffoldInit writes task title and description in context json`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(
            root, AgentProfile.STRICT,
            "My project context", "My vision",
            "My Title", "My Desc", emptyList(), emptyList()
        )

        val json = File(root, "context.json").readText()
        assertTrue(json.contains("\"title\":\"My Title\""))
        assertTrue(json.contains("\"description\":\"My Desc\""))
        assertTrue(json.contains("\"startedAt\""))
    }

    @Test
    fun `scaffoldInit writes vision in vision md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(
            root, AgentProfile.STRICT,
            "Context", "Ship a zero-friction DDD plugin",
            "First task", "Desc", emptyList(), emptyList()
        )

        val content = File(root, "vision.md").readText()
        assertTrue(content.contains("Ship a zero-friction DDD plugin"))
    }

    @Test
    fun `scaffoldInit creates step files`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(
            root, AgentProfile.STRICT,
            "Context", "Vision",
            "First task", "Desc", emptyList(),
            listOf("Phase 1 Core" to "Build the core structure", "Phase 2 UI" to "Build the UI")
        )

        assertTrue(File(root, "steps/phase-1-core.md").exists())
        assertTrue(File(root, "steps/phase-2-ui.md").exists())
    }

    @Test
    fun `scaffoldInit does not overwrite existing CONTRACT md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        root.mkdirs()
        File(root, "CONTRACT.md").writeText("# Mon contrat personnalisé")

        defaultInit(root)

        assertEquals("# Mon contrat personnalisé", File(root, "CONTRACT.md").readText())
    }

    @Test
    fun `scaffoldInit does not overwrite existing README md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        root.mkdirs()
        File(root, "README.md").writeText("# Mon README")

        defaultInit(root)

        assertEquals("# Mon README", File(root, "README.md").readText())
    }

    @Test
    fun `scaffoldInit does not overwrite existing CONTEXT md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        root.mkdirs()
        File(root, "CONTEXT.md").writeText("# Contexte permanent existant")

        defaultInit(root)

        assertTrue(File(root, "CONTEXT.md").readText().contains("Contexte permanent existant"),
            "CONTEXT.md is permanent — must not be overwritten")
    }

    @Test
    fun `scaffoldInit creates project README md if absent`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root)

        val readme = tmp.resolve("README.md").toFile()
        assertTrue(readme.exists(), "README.md should be created at project root")
        assertTrue(readme.readText().contains("For AI Agent"), "README.md should contain agent header")
        assertTrue(readme.readText().contains(".ai_context"), "README.md should reference .ai_context")
    }

    @Test
    fun `scaffoldInit does not overwrite existing project README md`(@TempDir tmp: Path) {
        val existing = tmp.resolve("README.md").toFile()
        existing.writeText("# Mon projet existant")

        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root)

        val content = existing.readText()
        assertTrue(content.contains("Mon projet existant"), "Original content must be preserved")
        assertTrue(content.contains(".ai_context"), "Agent header must be prepended")
        assertTrue(content.contains("For AI Agent"), "Agent header must be prepended")
    }

    @Test
    fun `scaffoldInit does not duplicate agent header if already present`(@TempDir tmp: Path) {
        val existing = tmp.resolve("README.md").toFile()
        existing.writeText("# For AI Agent :\n\nRead all [context](./.ai_context)\n\n# Mon projet")

        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root)

        val occurrences = existing.readText().split("For AI Agent").size - 1
        assertEquals(1, occurrences, "Agent header must not be duplicated")
    }

    // ── scaffoldNewTask ───────────────────────────────────────────────────────

    @Test
    fun `scaffoldNewTask clears tasks done directory`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root)
        File(root, "tasks/done/summary.md").writeText("# Done")

        TemplateProvider.scaffoldNewTask(root, "", "New task", "New desc", emptyList())

        assertFalse(File(root, "tasks/done/summary.md").exists())
    }

    @Test
    fun `scaffoldNewTask removes non-permanent files in tasks specification`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root)
        File(root, "tasks/specification/feature.md").writeText("# Spec")
        File(root, "tasks/specification/permanent-overview.md").writeText("# Overview")

        TemplateProvider.scaffoldNewTask(root, "", "New task", "Desc", emptyList())

        assertFalse(File(root, "tasks/specification/feature.md").exists())
        assertTrue(File(root, "tasks/specification/permanent-overview.md").exists())
    }

    @Test
    fun `scaffoldNewTask removes non-permanent files in tasks technical`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root)
        File(root, "tasks/technical/adr.md").writeText("# ADR")
        File(root, "tasks/technical/permanent-conventions.md").writeText("# Conventions")

        TemplateProvider.scaffoldNewTask(root, "", "New task", "Desc", emptyList())

        assertFalse(File(root, "tasks/technical/adr.md").exists())
        assertTrue(File(root, "tasks/technical/permanent-conventions.md").exists())
    }

    @Test
    fun `scaffoldNewTask appends to history json`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root, "Old context")

        TemplateProvider.scaffoldNewTask(root, "", "New context", "New desc", emptyList())

        val log = File(root, "history.json").readText()
        assertTrue(log.contains("Old context"))
        assertTrue(log.contains("endedAt"))
    }

    @Test
    fun `scaffoldNewTask records completed step in history json`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root, "Old task")

        TemplateProvider.scaffoldNewTask(root, "phase-1-core", "New task", "Desc", emptyList())

        val log = File(root, "history.json").readText()
        assertTrue(log.contains("phase-1-core"), "Completed step must appear in history")
    }

    @Test
    fun `scaffoldNewTask writes new context json`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        defaultInit(root)

        TemplateProvider.scaffoldNewTask(root, "", "Fresh start", "New desc", emptyList())

        val json = File(root, "context.json").readText()
        assertTrue(json.contains("Fresh start"))
    }

    @Test
    fun `scaffoldNewTask preserves vision md and steps`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(
            root, AgentProfile.STRICT,
            "Context", "My big vision",
            "First task", "Desc", emptyList(),
            listOf("Phase 1" to "Core")
        )

        TemplateProvider.scaffoldNewTask(root, "", "Next task", "Desc", emptyList())

        assertTrue(File(root, "vision.md").readText().contains("My big vision"),
            "vision.md must be preserved on new task")
        assertTrue(File(root, "steps/phase-1.md").exists(),
            "steps/ must be preserved on new task")
    }

    // ── contractMd ────────────────────────────────────────────────────────────

    @Test
    fun `contractMd strict declares profile and prohibitions`() {
        val content = TemplateProvider.contractMd(AgentProfile.STRICT)
        assertTrue(content.contains("strict"))
        assertTrue(content.contains("PROHIBITIONS"))
        assertTrue(content.contains("npm install"))
    }

    @Test
    fun `contractMd standard allows tests`() {
        val content = TemplateProvider.contractMd(AgentProfile.STANDARD)
        assertTrue(content.contains("standard"))
        assertTrue(content.contains("ng test") || content.contains("npm test"))
    }

    @Test
    fun `contractMd permissive allows terminal commands`() {
        val content = TemplateProvider.contractMd(AgentProfile.PERMISSIVE)
        assertTrue(content.contains("permissive"))
        assertTrue(content.contains("All terminal commands"))
    }

    @Test
    fun `all profiles forbid modifying files outside workspace`() {
        for (profile in AgentProfile.entries) {
            val content = TemplateProvider.contractMd(profile)
            assertTrue(
                content.contains("outside") || content.contains("hors du workspace"),
                "Profile $profile must forbid files outside workspace"
            )
        }
    }

    // ── contextMd ────────────────────────────────────────────────────────────

    @Test
    fun `contextMd contains title description and todos`() {
        val content = TemplateProvider.contextMd("Mon titre", "Ma description", listOf("Tâche 1", "Tâche 2"))
        assertTrue(content.contains("Mon titre"))
        assertTrue(content.contains("Ma description"))
        assertTrue(content.contains("- [ ] Tâche 1"))
        assertTrue(content.contains("- [ ] Tâche 2"))
    }

    @Test
    fun `contextMd handles empty todos with placeholder`() {
        val content = TemplateProvider.contextMd("Titre", "Desc", emptyList())
        assertTrue(content.contains("- [ ]"))
    }
}
