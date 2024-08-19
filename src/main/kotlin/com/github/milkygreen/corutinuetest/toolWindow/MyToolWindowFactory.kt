package com.github.milkygreen.corutinuetest.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.milkygreen.corutinuetest.MyBundle
import com.github.milkygreen.corutinuetest.services.MyProjectService
import com.intellij.openapi.application.EDT
import kotlinx.coroutines.*
import javax.swing.JButton


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        suspend fun simulateLongRunningTask(): String = withContext(Dispatchers.IO) {
            // 模拟耗时操作
            Thread.sleep(3000)
            "Task Completed"
        }

        private val service = toolWindow.project.service<MyProjectService>()
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val label = JBLabel(MyBundle.message("randomLabel", "?"))

            add(label)
            add(JButton(MyBundle.message("shuffle")).apply {
                addActionListener {
                    scope.launch{
                        val result = withContext(Dispatchers.IO) {
                            simulateLongRunningTask()
                        }
                        val num = withContext(Dispatchers.Default){service.getRandomNumber()}
                        launch(Dispatchers.EDT) {
                            label.text = result + num
                        }
                    }
                }
            })
        }
    }
}
