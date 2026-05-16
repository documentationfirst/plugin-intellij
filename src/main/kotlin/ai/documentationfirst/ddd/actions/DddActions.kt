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
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
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
            setTitle("Documentation First — Agent profile (1/5)")
            setCenterPanel(JScrollPane(list))
            addOkAction(); addCancelAction()
        }
        if (!profileDialog.showAndGet()) return
        val profile = profiles[list.selectedIndex.coerceAtLeast(0)]

        // Step 2: project context (permanent)
        val contextField = JBTextField(50).apply { emptyText.text = "e.g. VSCode plugin in TypeScript for DDD scaffolding" }
        val contextPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JLabel("Project context — permanent (what we build, stack, conventions):"))
            add(contextField)
        }
        val contextDialog = DialogBuilder(project).apply {
            setTitle("Init (2/5) — Project Context (permanent)")
            setCenterPanel(contextPanel); addOkAction(); addCancelAction()
        }
        if (!contextDialog.showAndGet()) return
        val projectContext = contextField.text.trim().ifBlank { "*(no context)*" }

        // Step 3: vision (semi-permanent)
        val visionField = JBTextField(50).apply { emptyText.text = "e.g. Ship a zero-friction DDD plugin for all major IDEs" }
        val visionPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JLabel("Product vision — semi-permanent (epic goals):"))
            add(visionField)
        }
        val visionDialog = DialogBuilder(project).apply {
            setTitle("Init (3/5) — Vision (semi-permanent)")
            setCenterPanel(visionPanel); addOkAction(); addCancelAction()
        }
        if (!visionDialog.showAndGet()) return
        val vision = visionField.text.trim().ifBlank { "*(no vision)*" }

        // Step 4: steps (loop)
        val steps = mutableListOf<Pair<String, String>>()
        while (true) {
            val nameField = JBTextField(40)
            val descField = JBTextField(40)
            val stepPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(JLabel("Step ${steps.size + 1} name (leave empty to finish):"))
                add(nameField)
                add(JLabel("Description (optional):"))
                add(descField)
            }
            val stepDialog = DialogBuilder(project).apply {
                setTitle("Init (4/5) — Add step / phase")
                setCenterPanel(stepPanel); addOkAction(); addCancelAction()
            }
            if (!stepDialog.showAndGet()) break
            val name = nameField.text.trim()
            if (name.isBlank()) break
            steps.add(name to descField.text.trim())
        }

        // Step 5: first task
        val input = askContextInput(project, "Init (5/5) — First Task") ?: return

        TemplateProvider.scaffoldInit(aiContextRoot, profile, projectContext, vision, input.title, input.description, input.todos, steps)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(aiContextRoot)?.refresh(true, true)
        DddToolWindowFactory.refresh(project)

        DddNotifications.showInfo(
            project, "DDD initialisé ✅",
            "Stack: <b>${DddDetector.check(project).stack.label}</b> — Profil: <b>${profile.name.lowercase()}</b>"
        )
    }

    override fun update(e: AnActionEvent) {
        val root = e.project?.basePath ?: return
        e.presentation.isEnabledAndVisible = !File(root, ".ai_context").exists()
    }
}

// ── New Vision ────────────────────────────────────────────────────────────────

class NewVisionAction : AnAction(AllIcons.Actions.Refresh) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val aiContextRoot = requireAiContextRoot(project) ?: return

        val confirm = Messages.showOkCancelDialog(
            project,
            "vision.md va être réécrite. steps/ va être réinitialisé. tasks/ (non-permanent) va être vidé.\n" +
            "CONTEXT.md et skills/ sont conservés.\n\nAssurez-vous d'avoir commité d'abord.",
            "⚠️ Nouvelle Vision",
            "Continuer quand même", "Annuler",
            Messages.getWarningIcon()
        )
        if (confirm != Messages.OK) return

        // New vision
        val visionField = JBTextField(50).apply { emptyText.text = "e.g. Expand to all major IDE platforms" }
        val visionPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JLabel("Nouvelle vision produit (semi-permanent):"))
            add(visionField)
        }
        val visionDialog = DialogBuilder(project).apply {
            setTitle("Nouvelle Vision (1/3)")
            setCenterPanel(visionPanel); addOkAction(); addCancelAction()
        }
        if (!visionDialog.showAndGet()) return
        val vision = visionField.text.trim().ifBlank { "*(no vision)*" }

        // Steps (loop)
        val steps = mutableListOf<Pair<String, String>>()
        while (true) {
            val nameField = JBTextField(40)
            val descField = JBTextField(40)
            val stepPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(JLabel("Step ${steps.size + 1} (laissez vide pour terminer):"))
                add(nameField)
                add(JLabel("Description:"))
                add(descField)
            }
            val stepDialog = DialogBuilder(project).apply {
                setTitle("Nouvelle Vision (2/3) — Steps")
                setCenterPanel(stepPanel); addOkAction(); addCancelAction()
            }
            if (!stepDialog.showAndGet()) break
            val name = nameField.text.trim()
            if (name.isBlank()) break
            steps.add(name to descField.text.trim())
        }

        // First task
        val input = askContextInput(project, "Nouvelle Vision (3/3) — Première tâche") ?: return

        TemplateProvider.scaffoldNewVision(aiContextRoot, vision, steps, input.title, input.description, input.todos)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(aiContextRoot)?.refresh(true, true)
        DddToolWindowFactory.refresh(project)

        DddNotifications.showInfo(project, "Nouvelle vision démarrée ✅", "<b>$vision</b>")
    }

    override fun update(e: AnActionEvent) {
        val root = e.project?.basePath ?: return
        e.presentation.isEnabledAndVisible = File(root, ".ai_context").exists()
    }
}

// ── New Task ──────────────────────────────────────────────────────────────────

class NewTaskAction : AnAction(AllIcons.Actions.Execute) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val aiContextRoot = requireAiContextRoot(project) ?: return

        val confirm = Messages.showOkCancelDialog(
            project,
            "tasks/ (non-permanent) va être vidé.\nvision.md, steps/, CONTEXT.md et skills/ sont conservés.\n\nAssurez-vous d'avoir commité d'abord.",
            "⚠️ Nouvelles Tasks",
            "Continuer quand même", "Annuler",
            Messages.getWarningIcon()
        )
        if (confirm != Messages.OK) return

        // Which step was completed? — read from dev-context.json for name consistency
        val devContextFile = File(aiContextRoot, "dev-context.json")
        data class StepItem(val name: String, val desc: String)
        val pendingSteps = mutableListOf<StepItem>()
        if (devContextFile.exists()) {
          try {
            val text = devContextFile.readText()
            val stepRegex = Regex(""""name"\s*:\s*"([^"]*?)"[^}]*?"description"\s*:\s*"([^"]*?)"[^}]*?"done"\s*:\s*(true|false)""")
            stepRegex.findAll(text).forEach { m ->
              if (m.groupValues[3] == "false") pendingSteps.add(StepItem(m.groupValues[1], m.groupValues[2]))
            }
          } catch (_: Exception) {}
        }

        val noneLabel = "(aucun) — aucun step achevé"
        val stepChoices = listOf(noneLabel) + pendingSteps.map { "${it.name}  —  ${it.desc}" }
        val stepList = JBList(stepChoices).apply { selectedIndex = 0 }
        val stepDialog = DialogBuilder(project).apply {
          setTitle("Nouvelle Task (1/3) — Un step vient-il d'être achevé ?")
          setCenterPanel(JScrollPane(stepList))
          addOkAction(); addCancelAction()
        }
        if (!stepDialog.showAndGet()) return
        val selectedLabel = stepList.selectedValue ?: noneLabel
        val completedStep = if (selectedLabel == noneLabel) "" else pendingSteps.getOrNull(stepList.selectedIndex - 1)?.name ?: ""

        // New task
        val input = askContextInput(project, "Nouvelles Tasks (2/3)") ?: return

        TemplateProvider.scaffoldNewTask(aiContextRoot, completedStep, input.title, input.description, input.todos)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(aiContextRoot)?.refresh(true, true)
        DddToolWindowFactory.refresh(project)

        val msg = if (completedStep.isNotBlank()) "Step achevé: <b>$completedStep</b> — Nouvelle tâche: <b>${input.title}</b>"
                  else "Nouvelle tâche: <b>${input.title}</b>"
        DddNotifications.showInfo(project, "Nouvelles tasks démarrées ✅", msg)
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

// ── New Skill (right-click on skills/ in tree) ────────────────────────────────

class NewSkillAction(private val skillsDir: File, private val project: com.intellij.openapi.project.Project) :
    AnAction("Nouveau skill .md", null, AllIcons.Nodes.Editorconfig) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val name = askInput(
            project,
            "Nom du skill",
            "ex: permanent-dev-typescript ou api-design-rules"
        ) ?: return
        val file = File(skillsDir, "$name.md")
        if (!file.exists()) {
            val displayName = name.removePrefix("permanent-")
            file.writeText(
                "# Skill — $displayName\n\n" +
                "*Créé : ${java.time.LocalDate.now()}*\n\n---\n\n" +
                "## Rôle\n\n\n\n## Règles\n\n\n"
            )
        }
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(skillsDir)?.refresh(true, true)
        openFile(project, file)
        DddToolWindowFactory.refresh(project)
    }
}

