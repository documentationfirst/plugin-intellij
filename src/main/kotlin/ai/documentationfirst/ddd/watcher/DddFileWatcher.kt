package ai.documentationfirst.ddd.watcher

import ai.documentationfirst.ddd.toolwindow.DddToolWindowFactory
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent
import com.intellij.openapi.wm.ToolWindowManager

/**
 * DddFileWatcher
 *
 * Listens to IntelliJ VirtualFileSystem events and automatically refreshes
 * the DDD Tool Window whenever a .md file is created, deleted, moved or
 * renamed inside .ai_context/.
 *
 * Registered via plugin.xml as a projectService + projectListener.
 */
@Service(Service.Level.PROJECT)
class DddFileWatcher(private val project: Project) : Disposable {

    private val connection = project.messageBus.connect()

    init {
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                val hasRelevantChange = events.any { event ->
                    isRelevantEvent(event)
                }
                if (hasRelevantChange) {
                    refreshToolWindow()
                }
            }
        })
    }

    private fun isRelevantEvent(event: VFileEvent): Boolean {
        val path = event.path
        // Only react to .md files inside .ai_context/
        if (!path.contains(".ai_context")) return false
        return when (event) {
            is VFileCreateEvent -> path.endsWith(".md")
            is VFileDeleteEvent -> path.endsWith(".md")
            is VFileMoveEvent -> path.endsWith(".md") || event.oldPath.endsWith(".md")
            is VFilePropertyChangeEvent -> {
                event.propertyName == VirtualFile.PROP_NAME &&
                        (path.endsWith(".md") || (event.oldValue as? String)?.endsWith(".md") == true)
            }
            else -> false
        }
    }

    private fun refreshToolWindow() {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Documentation first") ?: return@invokeLater
            // Force re-render of the tool window content
            toolWindow.contentManager.contents.forEach { content ->
                content.component.revalidate()
                content.component.repaint()
            }
            // Notify the factory to rebuild the file tree
            DddToolWindowFactory.refresh(project)
        }
    }

    override fun dispose() {
        connection.disconnect()
    }
}
