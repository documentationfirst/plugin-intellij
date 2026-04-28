package ai.documentationfirst.ddd.actions

import ai.documentationfirst.ddd.notifications.DddNotifications
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JScrollPane

/**
 * Shared utility functions for all DDD actions.
 */

fun openFile(project: Project, file: File) {
    val vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: return
    FileEditorManager.getInstance(project).openFile(vFile, true)
}

fun askInput(project: Project, label: String, hint: String): String? {
    val field = JBTextField(30).apply {
        emptyText.text = hint
    }
    val builder = DialogBuilder(project).apply {
        setTitle("Documentation First")
        setCenterPanel(
            JPanel(BorderLayout(0, 6)).apply {
                add(JLabel(label), BorderLayout.NORTH)
                add(field, BorderLayout.CENTER)
            }
        )
        addOkAction()
        addCancelAction()
        setOkOperation {
            val text = field.text.trim()
            if (text.isNotBlank() && text.matches(Regex("[a-zA-Z0-9\\-_ ]+"))) {
                dialogWrapper.close(0)
            }
        }
    }
    if (!builder.showAndGet()) return null
    return field.text.trim().ifBlank { null }
}

data class ContextInput(
    val title: String,
    val description: String,
    val todos: List<String>
)

fun askContextInput(project: Project, titleLabel: String = "Nouveau contexte"): ContextInput? {
    val titleField = JBTextField(30).apply { emptyText.text = "ex: Refonte authentification" }
    val descArea = JTextArea(3, 30).apply {
        lineWrap = true
        wrapStyleWord = true
        text = ""
    }
    val todosArea = JTextArea(5, 30).apply {
        lineWrap = true
        wrapStyleWord = true
        text = ""
    }

    val gbc = GridBagConstraints().apply {
        fill = GridBagConstraints.HORIZONTAL
        insets = Insets(4, 0, 4, 0)
        gridx = 0
        weightx = 1.0
    }
    val panel = JPanel(GridBagLayout()).apply {
        gbc.gridy = 0; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL
        add(JLabel("Titre *"), gbc)
        gbc.gridy = 1; add(titleField, gbc)
        gbc.gridy = 2; add(JLabel("Description"), gbc)
        gbc.gridy = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.3
        add(JScrollPane(descArea), gbc)
        gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0.0
        add(JLabel("Todo liste (une tâche par ligne)"), gbc)
        gbc.gridy = 5; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.7
        add(JScrollPane(todosArea), gbc)
    }

    val builder = DialogBuilder(project).apply {
        setTitle("Documentation First — $titleLabel")
        setCenterPanel(panel)
        addOkAction()
        addCancelAction()
        setOkOperation {
            if (titleField.text.trim().isNotBlank()) dialogWrapper.close(0)
        }
    }
    if (!builder.showAndGet()) return null
    val title = titleField.text.trim().ifBlank { return null }
    val description = descArea.text.trim()
    val todos = todosArea.text.lines().map { it.trim() }.filter { it.isNotBlank() }
    return ContextInput(title, description, todos)
}

/**
 * Returns the .ai_context root if it exists, or shows an error notification and returns null.
 */
fun requireAiContextRoot(project: Project): File? {
    val root = project.basePath ?: return null
    val aiContextRoot = File(root, ".ai_context")
    if (!aiContextRoot.exists()) {
        DddNotifications.showError(
            project,
            "Aucun dossier <code>.ai_context/</code> trouvé. Lancez <b>Initialize Context</b> d'abord."
        )
        return null
    }
    return aiContextRoot
}
