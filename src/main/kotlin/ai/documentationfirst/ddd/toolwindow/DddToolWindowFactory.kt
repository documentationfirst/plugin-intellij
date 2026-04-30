package ai.documentationfirst.ddd.toolwindow

import ai.documentationfirst.ddd.actions.NewDocumentAction
import ai.documentationfirst.ddd.actions.NewSkillAction
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
                add(makeToolbarButton("+ New context") {
                    ActionManager.getInstance().tryToExecute(
                        ActionManager.getInstance().getAction("ddd.newContext"),
                        null, topPanel, ActionPlaces.TOOLWINDOW_CONTENT, true
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
            // Right-click context menu on directories
            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mousePressed(e: java.awt.event.MouseEvent) {
                    if (!SwingUtilities.isRightMouseButton(e)) return
                    val path = getPathForLocation(e.x, e.y) ?: return
                    selectionPath = path
                    val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                    val file = node.userObject as? File ?: return
                    if (file.isDirectory) {
                        val isSkills = file.name == "skills"
                        val menu = JPopupMenu()
                        if (isSkills) {
                            menu.add(JMenuItem("New skill .md").apply {
                                icon = AllIcons.Nodes.Editorconfig
                                addActionListener {
                                    NewSkillAction(file, project).let { action ->
                                        ActionManager.getInstance().tryToExecute(
                                            action, null, null, ActionPlaces.POPUP, true
                                        )
                                    }
                                }
                            })
                        } else {
                            menu.add(JMenuItem("New file .md").apply {
                                icon = AllIcons.FileTypes.Text
                                addActionListener {
                                    NewDocumentAction(file, project).let { action ->
                                        ActionManager.getInstance().tryToExecute(
                                            action, null, null, ActionPlaces.POPUP, true
                                        )
                                    }
                                }
                            })
                        }
                        menu.show(this@apply, e.x, e.y)
                    }
                }
            })
        }
    }

    private fun buildTreeNode(): DefaultMutableTreeNode {
        val root = DefaultMutableTreeNode(".ai_context")
        val aiContextRoot = project.basePath?.let { File(it, ".ai_context") }
        if (aiContextRoot == null || !aiContextRoot.exists()) return root

        // Add documents/ subdirectory
        val documentsDir = File(aiContextRoot, "documents")
        if (documentsDir.exists()) {
            val documentsNode = DefaultMutableTreeNode(documentsDir)
            root.add(documentsNode)
            addChildren(documentsNode, documentsDir)
        }

        // Add skills/ subdirectory
        val skillsDir = File(aiContextRoot, "skills")
        if (skillsDir.exists()) {
            val skillsNode = DefaultMutableTreeNode(skillsDir)
            root.add(skillsNode)
            addChildren(skillsNode, skillsDir)
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

    private val todoRegex = Regex("""^- \[([ x])\] (.+)$""", RegexOption.MULTILINE)

    init {
        border = JBUI.Borders.empty(6)
        preferredSize = Dimension(0, 200)
    }

    fun load(aiContextRoot: File) {
        removeAll()
        val contextJson = File(aiContextRoot, "context.json")
        val contextMd = File(aiContextRoot, "CONTEXT.md")

        if (!contextJson.exists() || !contextMd.exists()) {
            add(JLabel("  No context initialized.", AllIcons.General.Information, SwingConstants.LEFT), BorderLayout.CENTER)
            revalidate(); repaint()
            return
        }

        val jsonText = contextJson.readText()
        val title = jsonText.extractJsonString("title") ?: "—"
        val startedAt = jsonText.extractJsonString("startedAt")?.take(10) ?: "—"
        val mdText = contextMd.readText()
        val description = mdText.substringAfter("## Description").substringBefore("---").trim()
            .lines().firstOrNull { it.isNotBlank() } ?: ""
        val todos = todoRegex.findAll(mdText).toList()

        val content = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        // Header
        content.add(JLabel("<html><b>$title</b> <font color='gray'>— $startedAt</font></html>").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyBottom(4)
        })
        if (description.isNotBlank()) {
            content.add(JLabel("<html><i>$description</i></html>").apply {
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
            for (match in todos) {
                val checked = match.groupValues[1] == "x"
                val taskText = match.groupValues[2]
                val cb = JCheckBox(taskText, checked).apply {
                    isOpaque = false
                    alignmentX = Component.LEFT_ALIGNMENT
                    if (checked) foreground = UIManager.getColor("Label.disabledForeground")
                    addActionListener {
                        toggleTodo(File(aiContextRoot, "CONTEXT.md"), taskText, isSelected)
                    }
                }
                content.add(cb)
            }
        }

        add(JBScrollPane(content).apply { border = null }, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun toggleTodo(contextMdFile: File, taskText: String, done: Boolean) {
        if (!contextMdFile.exists()) return
        val from = if (done) "- [ ] $taskText" else "- [x] $taskText"
        val to   = if (done) "- [x] $taskText" else "- [ ] $taskText"
        val updated = contextMdFile.readText().replace(from, to)
        contextMdFile.writeText(updated)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(contextMdFile)?.refresh(false, false)
    }

    private fun String.extractJsonString(key: String): String? {
        val regex = Regex(""""$key"\s*:\s*"([^"]*?)"""")
        return regex.find(this)?.groupValues?.get(1)
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
