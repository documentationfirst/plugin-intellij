package ai.documentationfirst.ddd.toolwindow

import ai.documentationfirst.ddd.actions.NewDocumentAction
import ai.documentationfirst.ddd.actions.NewSkillAction
import ai.documentationfirst.ddd.actions.NewTaskAction
import ai.documentationfirst.ddd.actions.NewVisionAction
import ai.documentationfirst.ddd.actions.Refresh
import ai.documentationfirst.ddd.actions.TogglePermanentAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.LayeredIcon
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel

// ── Tool Window Factory ───────────────────────────────────────────────────────

class DddToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = DddToolWindowPanel(project)
        registry[project] = panel
        val content = toolWindow.contentManager.factory.createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
        updateToolWindowIcon(project, toolWindow)
        Disposer.register(project) { unregister(project) }
    }

    companion object {
        private val baseIcon = IconLoader.getIcon("/assets/icon.svg", DddToolWindowFactory::class.java)

        private fun badgedIcon() = LayeredIcon(2).apply {
            setIcon(baseIcon, 0)
            setIcon(AllIcons.Nodes.ErrorMark, 1, 6, 0)
        }

        private fun updateToolWindowIcon(project: Project, toolWindow: ToolWindow) {
            val initialized = project.basePath?.let { File(it, ".ai_context").exists() } ?: false
            toolWindow.setIcon(if (initialized) baseIcon else badgedIcon())
        }

        private val registry = mutableMapOf<Project, DddToolWindowPanel>()

        fun refresh(project: Project) {
            registry[project]?.refresh()
            val toolWindow = com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
                .getToolWindow("Documentation first") ?: return
            updateToolWindowIcon(project, toolWindow)
        }

        fun unregister(project: Project) {
            registry.remove(project)
        }
    }
}

// ── Tool Window Panel ─────────────────────────────────────────────────────────

class DddToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(true) {

    private val tree: Tree
    private val topPanel = JPanel(BorderLayout())
    private val contextPanel = ContextPanel(project)

    init {
        tree = buildTree()

        setContent(JPanel(BorderLayout()).apply {
            add(topPanel, BorderLayout.NORTH)
            add(JBScrollPane(tree), BorderLayout.CENTER)
            add(contextPanel, BorderLayout.SOUTH)
        })

        updateTopPanel()
        updateContextPanel()
    }

    private fun makeToolbarButton(text: String, onClick: (AWTEvent) -> Unit): JButton =
        JButton(text).apply {
            isBorderPainted = false
            isOpaque = false
            isFocusPainted = false
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            font = font.deriveFont(Font.PLAIN, 12f)
            addActionListener { onClick(it) }
        }

    private fun updateTopPanel() {
        topPanel.removeAll()
        topPanel.border = JBUI.Borders.empty(4, 6)
        val initialized = project.basePath?.let { File(it, ".ai_context").exists() } ?: false

        if (initialized) {
            val buttonsPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                isOpaque = false
                add(makeToolbarButton("↺ Refresh") {
                    ActionManager.getInstance().tryToExecute(
                        Refresh(), null, topPanel, ActionPlaces.TOOLWINDOW_CONTENT, true
                    )
                })
                add(makeToolbarButton("⚡ New Vision") {
                    ActionManager.getInstance().tryToExecute(
                        NewVisionAction(), null, topPanel, ActionPlaces.TOOLWINDOW_CONTENT, true
                    )
                })
                add(makeToolbarButton("✔ New Task") {
                    ActionManager.getInstance().tryToExecute(
                        NewTaskAction(), null, topPanel, ActionPlaces.TOOLWINDOW_CONTENT, true
                    )
                })
            }
            topPanel.add(buttonsPanel, BorderLayout.WEST)
        } else {
            val btn = JButton("⚡ Initialize a context").apply {
                foreground = Color.WHITE
                background = Color(204, 102, 0)
                isBorderPainted = false
                isOpaque = true
                isFocusPainted = false
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                border = JBUI.Borders.empty(4, 10)
                addActionListener {
                    ActionManager.getInstance().tryToExecute(
                        ActionManager.getInstance().getAction("ddd.initContext"),
                        null, this, ActionPlaces.TOOLWINDOW_CONTENT, true
                    )
                }
            }
            topPanel.add(btn, BorderLayout.CENTER)
        }
        topPanel.revalidate()
        topPanel.repaint()
    }

    private fun buildTree(): Tree {
        val root = buildTreeNode()
        return Tree(DefaultTreeModel(root)).apply {
            isRootVisible = false
            showsRootHandles = true
            cellRenderer = DddTreeCellRenderer()
            addTreeSelectionListener { e ->
                val node = e.path?.lastPathComponent as? DefaultMutableTreeNode ?: return@addTreeSelectionListener
                val file = node.userObject as? File ?: return@addTreeSelectionListener
                if (file.isFile) {
                    val vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
                    if (vFile != null) FileEditorManager.getInstance(project).openFile(vFile, true)
                }
            }
            // Right-click context menu on directories and markdown files
            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mousePressed(e: java.awt.event.MouseEvent) {
                    if (!SwingUtilities.isRightMouseButton(e)) return
                    val path = getPathForLocation(e.x, e.y) ?: return
                    selectionPath = path
                    val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                    val file = node.userObject as? File ?: return
                    if (file.isDirectory) {
                        val isSkills = file.name == "skills"
                        val isTasks  = file.name == "tasks"
                        val menu = JPopupMenu()
                        when {
                            isSkills -> menu.add(JMenuItem("New skill .md").apply {
                                icon = AllIcons.Nodes.Editorconfig
                                addActionListener {
                                    NewSkillAction(file, project).let { action ->
                                        ActionManager.getInstance().tryToExecute(action, null, null, ActionPlaces.POPUP, true)
                                    }
                                }
                            })
                            isTasks -> menu.add(JMenuItem("↺ New Tasks (refresh)").apply {
                                icon = AllIcons.Actions.Execute
                                addActionListener {
                                    ActionManager.getInstance().tryToExecute(
                                        NewTaskAction(), null, null, ActionPlaces.POPUP, true
                                    )
                                }
                            })
                            else -> menu.add(JMenuItem("New file .md").apply {
                                icon = AllIcons.FileTypes.Text
                                addActionListener {
                                    NewDocumentAction(file, project).let { action ->
                                        ActionManager.getInstance().tryToExecute(action, null, null, ActionPlaces.POPUP, true)
                                    }
                                }
                            })
                        }
                        menu.show(this@apply, e.x, e.y)
                    } else if (isPermanentEligibleFile(file)) {
                        val menu = JPopupMenu()
                        menu.add(JMenuItem(if (file.name.startsWith("permanent-")) "Make non-permanent" else "Mark as permanent").apply {
                            icon = if (file.name.startsWith("permanent-")) AllIcons.Actions.Cancel else AllIcons.Actions.Commit
                            addActionListener {
                                TogglePermanentAction(file, project).let { action ->
                                    ActionManager.getInstance().tryToExecute(action, null, null, ActionPlaces.POPUP, true)
                                }
                            }
                        })
                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
        }
    }

    private fun isPermanentEligibleFile(file: File): Boolean {
        if (!file.isFile || file.extension != "md") return false
        val aiContextRoot = project.basePath?.let { File(it, ".ai_context") } ?: return false
        val parent = file.parentFile ?: return false
        val allowedDirs = listOf(
            File(aiContextRoot, "skills"),
            File(aiContextRoot, "tasks/done"),
            File(aiContextRoot, "tasks/specification"),
            File(aiContextRoot, "tasks/technical")
        )
        return allowedDirs.any { safeCanonical(parent) == safeCanonical(it) }
    }

    private fun safeCanonical(file: File): File = try {
        file.canonicalFile
    } catch (_: Exception) {
        file.absoluteFile
    }

    private fun buildTreeNode(): DefaultMutableTreeNode {
        val root = DefaultMutableTreeNode(".ai_context")
        val aiContextRoot = project.basePath?.let { File(it, ".ai_context") }
        if (aiContextRoot == null || !aiContextRoot.exists()) return root

        // Add tasks/, steps/, skills/ subdirectories
        for (dirName in listOf("tasks", "steps", "skills")) {
            val dir = File(aiContextRoot, dirName)
            if (dir.exists()) {
                val node = DefaultMutableTreeNode(dir)
                root.add(node)
                addChildren(node, dir)
            }
        }

        return root
    }

    private fun addChildren(parent: DefaultMutableTreeNode, dir: File) {
        val entries = dir.listFiles() ?: return
        val sorted = entries.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
        for (entry in sorted) {
            if (entry.name == ".gitkeep") continue
            val node = DefaultMutableTreeNode(entry)
            parent.add(node)
            if (entry.isDirectory) addChildren(node, entry)
        }
    }

    private fun updateContextPanel() {
        val root = project.basePath ?: return
        val aiContextRoot = File(root, ".ai_context")
        contextPanel.load(aiContextRoot)
    }

    fun refresh() {
        updateTopPanel()
        val newRoot = buildTreeNode()
        (tree.model as DefaultTreeModel).setRoot(newRoot)
        SwingUtilities.invokeLater {
            for (i in 0 until tree.rowCount) tree.expandRow(i)
        }
        updateContextPanel()
    }
}

// ── Context Panel (bottom) ────────────────────────────────────────────────────

class ContextPanel(private val project: Project) : JPanel(BorderLayout()) {

    init {
        border = JBUI.Borders.empty(6)
        preferredSize = Dimension(0, 200)
    }

    fun load(aiContextRoot: File) {
        removeAll()
        val devContextFile = File(aiContextRoot, "dev-context.json")

        if (!devContextFile.exists()) {
            add(JLabel("  No context initialized.", AllIcons.General.Information, SwingConstants.LEFT), BorderLayout.CENTER)
            revalidate(); repaint()
            return
        }

        val text = devContextFile.readText()
        val vision = Regex(""""vision"\s*:\s*"([^"]*?)"""").find(text)?.groupValues?.get(1) ?: ""
        val taskTitle = Regex(""""title"\s*:\s*"([^"]*?)"""").find(
            text.substringAfter("\"task\"")
        )?.groupValues?.get(1) ?: "—"
        val startedAt = Regex(""""startedAt"\s*:\s*"([^"T]*?)""").find(text)?.groupValues?.get(1) ?: "—"
        val taskDesc = Regex(""""description"\s*:\s*"([^"]*?)"""").find(
            text.substringAfter("\"task\"")
        )?.groupValues?.get(1) ?: ""

        // Parse steps
        data class StepInfo(val name: String, val done: Boolean)
        val stepRegex = Regex(""""name"\s*:\s*"([^"]*?)"[^}]*?"done"\s*:\s*(true|false)""")
        val steps = stepRegex.findAll(text.substringBefore("\"task\"")).map { m ->
            StepInfo(m.groupValues[1], m.groupValues[2] == "true")
        }.toList()

        // Parse todos
        data class TodoInfo(val text: String, val done: Boolean)
        val todoRegex = Regex(""""text"\s*:\s*"([^"]*?)"[^}]*?"done"\s*:\s*(true|false)""")
        val todos = todoRegex.findAll(text.substringAfter("\"todos\"")).map { m ->
            TodoInfo(m.groupValues[1], m.groupValues[2] == "true")
        }.toList()

        val content = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        // Vision
        if (vision.isNotBlank()) {
            content.add(JLabel("<html><font color='gray' style='font-size:9px'>VISION</font></html>").apply {
                alignmentX = Component.LEFT_ALIGNMENT
            })
            content.add(JLabel("<html><b>$vision</b></html>").apply {
                alignmentX = Component.LEFT_ALIGNMENT
                border = JBUI.Borders.emptyBottom(4)
            })
        }

        // Steps
        if (steps.isNotEmpty()) {
            content.add(JLabel("<html><font color='gray' style='font-size:9px'>STEPS</font></html>").apply {
                alignmentX = Component.LEFT_ALIGNMENT
            })
            for (step in steps) {
                val icon = if (step.done) "✅" else "○"
                content.add(JLabel("<html>$icon ${step.name}</html>").apply {
                    alignmentX = Component.LEFT_ALIGNMENT
                    if (step.done) foreground = UIManager.getColor("Label.disabledForeground")
                    border = JBUI.Borders.emptyBottom(1)
                })
            }
            content.add(Box.createVerticalStrut(6))
        }

        // Task
        content.add(JLabel("<html><font color='gray' style='font-size:9px'>TASK — $startedAt</font></html>").apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })
        content.add(JLabel("<html><b>$taskTitle</b></html>").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyBottom(2)
        })
        if (taskDesc.isNotBlank()) {
            content.add(JLabel("<html><i>$taskDesc</i></html>").apply {
                alignmentX = Component.LEFT_ALIGNMENT
                foreground = UIManager.getColor("Label.disabledForeground")
                border = JBUI.Borders.emptyBottom(6)
            })
        }

        // Todos
        if (todos.isNotEmpty()) {
            content.add(JLabel("Todo :").apply {
                alignmentX = Component.LEFT_ALIGNMENT
                font = font.deriveFont(Font.BOLD)
                border = JBUI.Borders.emptyBottom(2)
            })
            for (todo in todos) {
                val cb = JCheckBox(todo.text, todo.done).apply {
                    isOpaque = false
                    alignmentX = Component.LEFT_ALIGNMENT
                    if (todo.done) foreground = UIManager.getColor("Label.disabledForeground")
                    addActionListener {
                        toggleTodo(devContextFile, todo.text, isSelected)
                    }
                }
                content.add(cb)
            }
        }

        add(JBScrollPane(content).apply { border = null }, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun toggleTodo(devContextFile: File, taskText: String, done: Boolean) {
        if (!devContextFile.exists()) return
        val text = devContextFile.readText()
        val escaped = taskText.replace("\"", "\\\"")
        val from = if (done) """"text":"$escaped","done":false""" else """"text":"$escaped","done":true"""
        val to   = if (done) """"text":"$escaped","done":true"""  else """"text":"$escaped","done":false"""
        devContextFile.writeText(text.replace(from, to))
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(devContextFile)?.refresh(false, false)
    }
}

// ── Cell Renderer ─────────────────────────────────────────────────────────────

class DddTreeCellRenderer : DefaultTreeCellRenderer() {
    override fun getTreeCellRendererComponent(
        tree: JTree, value: Any, sel: Boolean, expanded: Boolean,
        leaf: Boolean, row: Int, hasFocus: Boolean
    ): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

        val node = value as? DefaultMutableTreeNode ?: return this
        val file = node.userObject as? File ?: return this

        val parentName = (node.parent as? DefaultMutableTreeNode)?.let {
            (it.userObject as? File)?.name
        }
        val isPermanent = file.name.startsWith("permanent-")
        val isDone = parentName == "done"

        text = if (isPermanent) file.name.removePrefix("permanent-") else file.name

        icon = when {
            file.isDirectory && file.name == "skills" -> AllIcons.Nodes.Editorconfig
            file.isDirectory && file.name == "steps"  -> AllIcons.Nodes.ModelClass
            file.isDirectory && file.name == "tasks"  -> AllIcons.Actions.Execute
            file.isDirectory -> AllIcons.Nodes.Folder
            file.extension == "md" -> AllIcons.FileTypes.Text
            else -> AllIcons.FileTypes.Unknown
        }

        when {
            isPermanent -> {
                font = font.deriveFont(Font.BOLD)
                foreground = Color(140, 82, 200) // violet
            }
            isDone -> {
                foreground = UIManager.getColor("Label.disabledForeground") ?: Color.GRAY
            }
            else -> {
                // default — let super handle it
            }
        }

        return this
    }
}
