package ai.documentationfirst.ddd.actions

import ai.documentationfirst.ddd.detector.DddDetector
import ai.documentationfirst.ddd.notifications.DddNotifications
import ai.documentationfirst.ddd.templates.AgentProfile
import ai.documentationfirst.ddd.templates.TemplateProvider
import ai.documentationfirst.ddd.toolwindow.DddToolWindowFactory
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBList
import java.io.File
import javax.swing.JScrollPane

// ── Initialize Context ────────────────────────────────────────────────────────

class InitContextAction : AnAction(AllIcons.Actions.AddFile) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val root = project.basePath ?: return
        val aiContextRoot = File(root, ".ai_context")

        // Step 1: choose agent profile
        val profiles = AgentProfile.entries.toTypedArray()
        val options = profiles.map { "${it.label} — ${it.description}" }.toTypedArray()
        val list = JBList(options.toList()).apply { selectedIndex = 0 }
        val profileDialog = DialogBuilder(project).apply {
            setTitle("Documentation First — Profil agent")
            setCenterPanel(JScrollPane(list))
            addOkAction()
            addCancelAction()
        }
        if (!profileDialog.showAndGet()) return
        val profile = profiles[list.selectedIndex.coerceAtLeast(0)]

        // Step 2: ask context info
        val input = askContextInput(project, "Initialiser le contexte") ?: return

        // Step 3: scaffold
        TemplateProvider.scaffoldInit(aiContextRoot, profile, input.title, input.description, input.todos)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(aiContextRoot)?.refresh(true, true)
        DddToolWindowFactory.refresh(project)

        DddNotifications.showInfo(
            project,
            "Contexte initialisé ✅",
            "Stack: <b>${DddDetector.check(project).stack.label}</b> — Profil: <b>${profile.name.lowercase()}</b>"
        )
    }

    override fun update(e: AnActionEvent) {
        val root = e.project?.basePath ?: return
        e.presentation.isEnabledAndVisible = !File(root, ".ai_context").exists()
    }
}

// ── New Context ───────────────────────────────────────────────────────────────

class NewContextAction : AnAction(AllIcons.Actions.Refresh) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val aiContextRoot = requireAiContextRoot(project) ?: return

        // Step 1: Git warning
        val confirm = Messages.showOkCancelDialog(
            project,
            """
            Les fichiers CONTEXT.md, context.json et le contenu de documents/ vont être effacés
            (sauf les fichiers permanent-*).
            
            Assurez-vous d'avoir commité ces fichiers dans Git si vous souhaitez conserver ce contexte.
            """.trimIndent(),
            "⚠️ Nouveau contexte",
            "Continuer quand même",
            "Annuler",
            Messages.getWarningIcon()
        )
        if (confirm != Messages.OK) return

        // Step 2: ask new context info
        val input = askContextInput(project, "Nouveau contexte") ?: return

        // Step 3: switch context
        TemplateProvider.scaffoldNewContext(aiContextRoot, input.title, input.description, input.todos)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(aiContextRoot)?.refresh(true, true)
        DddToolWindowFactory.refresh(project)

        DddNotifications.showInfo(
            project,
            "Nouveau contexte démarré ✅",
            "<b>${input.title}</b>"
        )
    }

    override fun update(e: AnActionEvent) {
        val root = e.project?.basePath ?: return
        e.presentation.isEnabledAndVisible = File(root, ".ai_context").exists()
    }
}

// ── New Document (right-click in tree) ───────────────────────────────────────

class NewDocumentAction(private val targetDir: File, private val project: com.intellij.openapi.project.Project) :
    AnAction("Nouveau fichier .md", null, AllIcons.FileTypes.Text) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val name = askInput(project, "Nom du fichier", "ex: my-decision ou permanent-conventions") ?: return
        val file = File(targetDir, "$name.md")
        if (!file.exists()) file.writeText("# $name\n\n*Créé : ${java.time.LocalDate.now()}*\n\n---\n\n")
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetDir)?.refresh(true, true)
        openFile(project, file)
        DddToolWindowFactory.refresh(project)
    }
}
