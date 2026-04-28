package ai.documentationfirst.ddd.startup

import ai.documentationfirst.ddd.detector.DddDetector
import ai.documentationfirst.ddd.notifications.DddNotifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class DddProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val result = DddDetector.check(project)
        if (result.hasDddFolder) {
            DddNotifications.showDddReady(project, result.stack)
        } else {
            DddNotifications.showInitPrompt(project)
        }
    }
}
