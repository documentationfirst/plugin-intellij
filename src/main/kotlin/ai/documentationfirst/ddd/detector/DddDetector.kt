package ai.documentationfirst.ddd.detector

import com.intellij.openapi.project.Project
import java.io.File
import java.nio.file.Files

enum class Stack(val label: String) {
    ANGULAR("Angular"),
    REACT("React"),
    VUE("Vue"),
    SPRING_BOOT("Spring Boot"),
    PYTHON("Python"),
    RUST("Rust"),
    GO("Go"),
    GENERIC("Generic"),
}

data class DetectionResult(
    val hasDddFolder: Boolean,
    val stack: Stack,
)

object DddDetector {

    fun check(project: Project): DetectionResult {
        val root = project.basePath ?: return DetectionResult(false, Stack.GENERIC)
        val rootFile = File(root)
        val hasDddFolder = File(root, ".ai_context").exists()
        val stack = detectStack(rootFile)
        return DetectionResult(hasDddFolder, stack)
    }

    private fun detectStack(root: File): Stack {
        // JS / TS stacks — read package.json
        val packageJson = File(root, "package.json")
        if (packageJson.exists()) {
            val content = packageJson.takeIf { it.length() < 1_000_000 }?.readText() ?: ""
            return when {
                content.contains("\"@angular/core\"") -> Stack.ANGULAR
                content.contains("\"react\"") -> Stack.REACT
                content.contains("\"vue\"") -> Stack.VUE
                else -> Stack.GENERIC
            }
        }
        if (File(root, "pom.xml").exists()) {
            val pomContent = File(root, "pom.xml").readText()
            return if (pomContent.contains("spring-boot")) Stack.SPRING_BOOT else Stack.GENERIC
        }
        val gradleKts = File(root, "build.gradle.kts")
        val gradle = File(root, "build.gradle")
        if (gradleKts.exists() || gradle.exists()) {
            val gradleContent = (if (gradleKts.exists()) gradleKts else gradle).readText()
            return if (gradleContent.contains("spring-boot")) Stack.SPRING_BOOT else Stack.GENERIC
        }
        if (File(root, "Cargo.toml").exists()) return Stack.RUST
        if (File(root, "go.mod").exists()) return Stack.GO
        if (File(root, "requirements.txt").exists() ||
            File(root, "pyproject.toml").exists()) {
            return Stack.PYTHON
        }
        return Stack.GENERIC
    }
}
