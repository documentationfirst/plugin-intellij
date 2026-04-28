package ai.documentationfirst.ddd

import ai.documentationfirst.ddd.templates.AgentProfile
import ai.documentationfirst.ddd.templates.TemplateProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class TemplateProviderTest {

    // ── scaffoldInit ──────────────────────────────────────────────────────────

    @Test
    fun `scaffoldInit creates the full directory structure`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Mon contexte", "Description", listOf("Tâche 1"))

        assertTrue(root.exists())
        assertTrue(File(root, "README.md").exists())
        assertTrue(File(root, "CONTRACT.md").exists())
        assertTrue(File(root, "CONTEXT.md").exists())
        assertTrue(File(root, "context.json").exists())
        assertTrue(File(root, "documents/done").exists())
        assertTrue(File(root, "documents/specification").exists())
        assertTrue(File(root, "documents/technical").exists())
    }

    @Test
    fun `scaffoldInit writes correct title in CONTEXT md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Refonte auth", "Objectif", listOf("Étape 1"))

        val content = File(root, "CONTEXT.md").readText()
        assertTrue(content.contains("Refonte auth"))
        assertTrue(content.contains("Étape 1"))
        assertTrue(content.contains("- [ ]"))
    }

    @Test
    fun `scaffoldInit writes correct json`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "My Title", "My Desc", emptyList())

        val json = File(root, "context.json").readText()
        assertTrue(json.contains("\"title\":\"My Title\""))
        assertTrue(json.contains("\"description\":\"My Desc\""))
        assertTrue(json.contains("\"startedAt\""))
    }

    @Test
    fun `scaffoldInit does not overwrite existing CONTRACT md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        root.mkdirs()
        File(root, "CONTRACT.md").writeText("# Mon contrat personnalisé")

        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Titre", "Desc", emptyList())

        assertEquals("# Mon contrat personnalisé", File(root, "CONTRACT.md").readText())
    }

    @Test
    fun `scaffoldInit does not overwrite existing README md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        root.mkdirs()
        File(root, "README.md").writeText("# Mon README")

        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Titre", "Desc", emptyList())

        assertEquals("# Mon README", File(root, "README.md").readText())
    }

    @Test
    fun `scaffoldInit always overwrites CONTEXT md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        root.mkdirs()
        File(root, "CONTEXT.md").writeText("# Ancien contexte")

        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Nouveau", "Desc", emptyList())

        assertTrue(File(root, "CONTEXT.md").readText().contains("Nouveau"))
    }

    @Test
    fun `scaffoldInit creates project README md if absent`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Titre", "Desc", emptyList())

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
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Titre", "Desc", emptyList())

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
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Titre", "Desc", emptyList())

        val occurrences = existing.readText().split("For AI Agent").size - 1
        assertEquals(1, occurrences, "Agent header must not be duplicated")
    }

    // ── scaffoldNewContext ────────────────────────────────────────────────────

    @Test
    fun `scaffoldNewContext clears done directory`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Old", "Desc", emptyList())
        File(root, "documents/done/summary.md").writeText("# Done")

        TemplateProvider.scaffoldNewContext(root, "New", "New desc", emptyList())

        assertFalse(File(root, "documents/done/summary.md").exists())
    }

    @Test
    fun `scaffoldNewContext removes non-permanent files in specification`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Old", "Desc", emptyList())
        File(root, "documents/specification/feature.md").writeText("# Spec")
        File(root, "documents/specification/permanent-overview.md").writeText("# Overview")

        TemplateProvider.scaffoldNewContext(root, "New", "Desc", emptyList())

        assertFalse(File(root, "documents/specification/feature.md").exists())
        assertTrue(File(root, "documents/specification/permanent-overview.md").exists())
    }

    @Test
    fun `scaffoldNewContext removes non-permanent files in technical`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Old", "Desc", emptyList())
        File(root, "documents/technical/adr.md").writeText("# ADR")
        File(root, "documents/technical/permanent-conventions.md").writeText("# Conventions")

        TemplateProvider.scaffoldNewContext(root, "New", "Desc", emptyList())

        assertFalse(File(root, "documents/technical/adr.md").exists())
        assertTrue(File(root, "documents/technical/permanent-conventions.md").exists())
    }

    @Test
    fun `scaffoldNewContext appends to history log`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Old context", "Desc", emptyList())

        TemplateProvider.scaffoldNewContext(root, "New context", "New desc", emptyList())

        val log = File(root, "history.log").readText()
        assertTrue(log.contains("Old context"))
        assertTrue(log.contains("endedAt"))
    }

    @Test
    fun `scaffoldNewContext writes new CONTEXT md`(@TempDir tmp: Path) {
        val root = tmp.resolve(".ai_context").toFile()
        TemplateProvider.scaffoldInit(root, AgentProfile.STRICT, "Old", "Desc", emptyList())

        TemplateProvider.scaffoldNewContext(root, "Fresh start", "New desc", listOf("Task A"))

        val content = File(root, "CONTEXT.md").readText()
        assertTrue(content.contains("Fresh start"))
        assertTrue(content.contains("Task A"))
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
        assertTrue(content.contains("build") && content.contains("install"))
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
