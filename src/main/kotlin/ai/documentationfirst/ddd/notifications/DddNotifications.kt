package ai.documentationfirst.ddd.notifications

import ai.documentationfirst.ddd.detector.Stack
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager

object DddNotifications {

    private const val GROUP_ID = "DDD Notifications"

    fun showDddReady(project: Project, stack: Stack) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(
                "Documentation First - Ready ✅",
                ".ai_context/ detected - stack: <b>${stack.label}</b>",
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    fun showInitPrompt(project: Project) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(
                "Documentation First - No context",
                "No <code>.ai_context/</code> folder found in this project. Initialize?",
                NotificationType.WARNING
            )

        notification.addAction(object : AnAction("Initialize") {
            override fun actionPerformed(e: AnActionEvent) {
                notification.expire()
                val frame = WindowManager.getInstance().getFrame(project) ?: return
                val action = ActionManager.getInstance().getAction("ddd.initContext")
                ActionManager.getInstance().tryToExecute(action, null, frame, ActionPlaces.NOTIFICATION, true)
            }
        })

        notification.notify(project)
    }

    fun showInfo(project: Project, title: String, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(title, message, NotificationType.INFORMATION)
            .notify(project)
    }

    fun showError(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification("Documentation First - Error", message, NotificationType.ERROR)
            .notify(project)
    }
}
