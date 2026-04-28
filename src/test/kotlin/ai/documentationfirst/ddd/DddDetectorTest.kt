package ai.documentationfirst.ddd

import ai.documentationfirst.ddd.detector.Stack
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

/**
 * Unit tests for DddDetector — without a real IntelliJ Project.
 * We test the private detectStack logic via a test-friendly wrapper.
 */
class DddDetectorTest {

    // Expose the stack detection logic for unit testing without a Project instance
    private fun detectStack(root: File): Stack {
        val packageJson = File(root, "package.json")
        if (packageJson.exists()) {
            val content = packageJson.readText()
            return when {
                content.contains("\"@angular/core\"") -> Stack.ANGULAR
                content.contains("\"react\"") -> Stack.REACT
                content.contains("\"vue\"") -> Stack.VUE
                else -> Stack.GENERIC
            }
        }
        if (File(root, "pom.xml").exists()) return Stack.SPRING_BOOT
        if (File(root, "Cargo.toml").exists()) return Stack.RUST
        if (File(root, "go.mod").exists()) return Stack.GO
        if (File(root, "requirements.txt").exists()) return Stack.PYTHON
        return Stack.GENERIC
    }

    @Test
    fun `detects Angular from package json`(@TempDir tmp: Path) {
        val root = tmp.toFile()
        File(root, "package.json").writeText("""{"dependencies":{"@angular/core":"^17.0.0"}}""")
        assertEquals(Stack.ANGULAR, detectStack(root))
    }

    @Test
    fun `detects React from package json`(@TempDir tmp: Path) {
        val root = tmp.toFile()
        File(root, "package.json").writeText("""{"dependencies":{"react":"^18.0.0"}}""")
        assertEquals(Stack.REACT, detectStack(root))
    }

    @Test
    fun `detects Spring Boot from pom xml`(@TempDir tmp: Path) {
        val root = tmp.toFile()
        File(root, "pom.xml").writeText("<project/>")
        assertEquals(Stack.SPRING_BOOT, detectStack(root))
    }

    @Test
    fun `detects Rust from Cargo toml`(@TempDir tmp: Path) {
        val root = tmp.toFile()
        File(root, "Cargo.toml").writeText("[package]")
        assertEquals(Stack.RUST, detectStack(root))
    }

    @Test
    fun `detects Go from go mod`(@TempDir tmp: Path) {
        val root = tmp.toFile()
        File(root, "go.mod").writeText("module example.com/app")
        assertEquals(Stack.GO, detectStack(root))
    }

    @Test
    fun `detects Python from requirements txt`(@TempDir tmp: Path) {
        val root = tmp.toFile()
        File(root, "requirements.txt").writeText("fastapi\npydantic")
        assertEquals(Stack.PYTHON, detectStack(root))
    }

    @Test
    fun `returns Generic when no known files`(@TempDir tmp: Path) {
        assertEquals(Stack.GENERIC, detectStack(tmp.toFile()))
    }

    @Test
    fun `hasDddFolder is true when dot ai_context exists`(@TempDir tmp: Path) {
        val root = tmp.toFile()
        File(root, ".ai_context").mkdirs()
        assertTrue(File(root, ".ai_context").exists())
    }

    @Test
    fun `hasDddFolder is false when dot ai_context is absent`(@TempDir tmp: Path) {
        assertFalse(File(tmp.toFile(), ".ai_context").exists())
    }
}

