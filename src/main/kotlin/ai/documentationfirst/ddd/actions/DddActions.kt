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
import javax.swing.ListSelectionModel

// ── Initialize Context ────────────────────────────────────────────────────────

class InitContextAction : AnAction(AllIcons.Actions.AddFile) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val root = project.basePath ?: return
        val aiContextRoot = File(root, ".ai_context")

        // Step 0: Full or Quick init?
        val modeChoices = listOf("⭐ Full init (recommended) — Profile, Context, Vision, Steps, First Task",
                                 "⚡ Quick init — Profile + Context only (fill vision & steps later)")
        val modeList = JBList(modeChoices).apply { selectedIndex = 0 }
        val modeDialog = DialogBuilder(project).apply {
            setTitle("Documentation First — Init mode")
            setCenterPanel(JScrollPane(modeList)); addOkAction(); addCancelAction()
        }
        if (!modeDialog.showAndGet()) return
        val isQuick = modeList.selectedIndex == 1

        // Step 1: choose agent profile
        val profiles = AgentProfile.entries.toTypedArray()
        val options = profiles.map { "${it.label} — ${it.description}" }.toTypedArray()
        val list = JBList(options.toList()).apply { selectedIndex = 0 }
        val profileDialog = DialogBuilder(project).apply {
            setTitle(if (isQuick) "Quick Init (1/3) — Agent profile" else "Full Init (1/5) — Agent profile")
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
            setTitle(if (isQuick) "Quick Init (2/3) — Project Context" else "Full Init (2/5) — Project Context (permanent)")
            setCenterPanel(contextPanel); addOkAction(); addCancelAction()
        }
        if (!contextDialog.showAndGet()) return
        val projectContext = contextField.text.trim().ifBlank { "*(no context)*" }

        // Steps 3+4: vision & steps — skipped in Quick mode
        var vision = "*(To be defined — fill vision.md before asking the agent to work)*"
        val steps = mutableListOf<Pair<String, String>>()

        if (!isQuick) {
            // Step 3: vision
            val visionField = JBTextField(50).apply { emptyText.text = "e.g. Ship a zero-friction DDD plugin for all major IDEs" }
            val visionPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(JLabel("Product vision — semi-permanent (epic goals):"))
                add(visionField)
            }
            val visionDialog = DialogBuilder(project).apply {
                setTitle("Full Init (3/5) — Vision (semi-permanent)")
                setCenterPanel(visionPanel); addOkAction(); addCancelAction()
            }
            if (!visionDialog.showAndGet()) return
            vision = visionField.text.trim().ifBlank { "*(no vision)*" }

            // Step 4: steps (loop)
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
                    setTitle("Full Init (4/5) — Add step / phase")
                    setCenterPanel(stepPanel); addOkAction(); addCancelAction()
                }
                if (!stepDialog.showAndGet()) break
                val name = nameField.text.trim()
                if (name.isBlank()) break
                steps.add(name to descField.text.trim())
            }
        }

        // Last step: first task
        val taskStepNum = if (isQuick) 3 else 5
        val taskTotal = if (isQuick) 3 else 5
        val input = askContextInput(project, "${if (isQuick) "Quick" else "Full"} Init ($taskStepNum/$taskTotal) — First Task") ?: return

        TemplateProvider.scaffoldInit(aiContextRoot, profile, projectContext, vision, input.title, input.description, input.todos, steps)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(aiContextRoot)?.refresh(true, true)
        DddToolWindowFactory.refresh(project)

        DddNotifications.showInfo(
            project, "DDD initialized ✅",
            "Stack: <b>${DddDetector.check(project).stack.label}</b> — Profile: <b>${profile.name.lowercase()}</b>" +
            if (isQuick) " — Quick mode: fill vision.md before working with the agent" else ""
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
            "vision.md will be rewritten. steps/ will be reset. tasks/ (non-permanent) will be cleared.\n" +
            "CONTEXT.md and skills/ are preserved.\n\nMake sure you have committed first.",
            "⚠️ New Vision",
            "Continue anyway", "Cancel",
            Messages.getWarningIcon()
        )
        if (confirm != Messages.OK) return

        // New vision
        val visionField = JBTextField(50).apply { emptyText.text = "e.g. Expand to all major IDE platforms" }
        val visionPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JLabel("New product vision (semi-permanent):"))
            add(visionField)
        }
        val visionDialog = DialogBuilder(project).apply {
            setTitle("New Vision (1/3)")
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
                add(JLabel("Step ${steps.size + 1} (leave empty to finish):"))
                add(nameField)
                add(JLabel("Description:"))
                add(descField)
            }
            val stepDialog = DialogBuilder(project).apply {
                setTitle("New Vision (2/3) — Steps")
                setCenterPanel(stepPanel); addOkAction(); addCancelAction()
            }
            if (!stepDialog.showAndGet()) break
            val name = nameField.text.trim()
            if (name.isBlank()) break
            steps.add(name to descField.text.trim())
        }

        // First task
        val input = askContextInput(project, "New Vision (3/3) — First task") ?: return

        TemplateProvider.scaffoldNewVision(aiContextRoot, vision, steps, input.title, input.description, input.todos)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(aiContextRoot)?.refresh(true, true)
        DddToolWindowFactory.refresh(project)

        DddNotifications.showInfo(project, "New vision started ✅", "<b>$vision</b>")
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
            "tasks/ (non-permanent) will be cleared.\n\nMake sure you have committed first.",
            "⚠️ New Tasks",
            "Continue anyway", "Cancel",
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

        val noneLabel = "(none) — no step completed"
        val stepChoices = listOf(noneLabel) + pendingSteps.map { "${it.name}  —  ${it.desc}" }
        val stepList = JBList(stepChoices).apply { selectedIndex = 0 }
        val stepDialog = DialogBuilder(project).apply {
          setTitle("New Task (1/4) — Was a step just completed?")
          setCenterPanel(JScrollPane(stepList))
          addOkAction(); addCancelAction()
        }
        if (!stepDialog.showAndGet()) return
        val selectedLabel = stepList.selectedValue ?: noneLabel
        val completedStep = if (selectedLabel == noneLabel) "" else pendingSteps.getOrNull(stepList.selectedIndex - 1)?.name ?: ""

        // Which spec files to delete? — multi-select, all checked by default
        val specDir = File(aiContextRoot, "tasks/specification")
        val specsToDelete = mutableListOf<String>()
        if (specDir.exists()) {
            val specFiles = specDir.listFiles()
                ?.filter { it.name != ".gitkeep" && !it.name.startsWith("permanent-") && it.name.endsWith(".md") }
                ?.map { it.name }
                ?: emptyList()
            if (specFiles.isNotEmpty()) {
                val specList = JBList(specFiles).apply {
                    selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
                    // select all by default
                    setSelectionInterval(0, specFiles.size - 1)
                }
                val specDialog = DialogBuilder(project).apply {
                    setTitle("New Task (2/4) — Which spec files to delete? (deselect to keep)")
                    setCenterPanel(JScrollPane(specList))
                    addOkAction(); addCancelAction()
                }
                if (!specDialog.showAndGet()) return
                specsToDelete.addAll(specList.selectedValuesList)
            }
        }

        // New task
        val input = askContextInput(project, "New Task (3/4)") ?: return

        TemplateProvider.scaffoldNewTask(aiContextRoot, completedStep, input.title, input.description, input.todos, specsToDelete)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(aiContextRoot)?.refresh(true, true)
        DddToolWindowFactory.refresh(project)

        val msg = if (completedStep.isNotBlank()) "Step completed: <b>$completedStep</b> — New task: <b>${input.title}</b>"
                  else "New task: <b>${input.title}</b>"
        DddNotifications.showInfo(project, "New tasks started ✅", msg)
    }

    override fun update(e: AnActionEvent) {
        val root = e.project?.basePath ?: return
        e.presentation.isEnabledAndVisible = File(root, ".ai_context").exists()
    }
}

// ── New Document (right-click in tree) ───────────────────────────────────────

class NewDocumentAction(private val targetDir: File, private val project: com.intellij.openapi.project.Project) :
    AnAction("New .md file", null, AllIcons.FileTypes.Text) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val name = askInput(project, "File name", "e.g. my-decision or permanent-conventions") ?: return
        val file = File(targetDir, "$name.md")
        if (!file.exists()) file.writeText("# $name\n\n*Created: ${java.time.LocalDate.now()}*\n\n---\n\n")
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetDir)?.refresh(true, true)
        openFile(project, file)
        DddToolWindowFactory.refresh(project)
    }
}

// ── New Skill (right-click on skills/ in tree) ────────────────────────────────

class NewSkillAction(private val skillsDir: File, private val project: com.intellij.openapi.project.Project) :
    AnAction("New skill .md", null, AllIcons.Nodes.Editorconfig) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val name = askInput(
            project,
            "Skill name",
            "e.g. permanent-dev-typescript or api-design-rules"
        ) ?: return
        val file = File(skillsDir, "$name.md")
        if (!file.exists()) {
            val displayName = name.removePrefix("permanent-")
            file.writeText(
                "# Skill — $displayName\n\n" +
                "*Created: ${java.time.LocalDate.now()}*\n\n---\n\n" +
                "## Role\n\n\n\n## Rules\n\n\n"
            )
        }
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(skillsDir)?.refresh(true, true)
        openFile(project, file)
        DddToolWindowFactory.refresh(project)
    }
}

