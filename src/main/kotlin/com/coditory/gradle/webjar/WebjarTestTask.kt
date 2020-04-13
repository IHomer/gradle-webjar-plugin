package com.coditory.gradle.webjar

import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_INSTALL_TASK
import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_TASK_GROUP
import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_TEST_TASK
import com.coditory.gradle.webjar.shared.ProjectFiles.filterExistingDirs
import com.coditory.gradle.webjar.shared.TimeMarkers.createTimeMarkerFile
import com.moowork.gradle.node.npm.NpmTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME

object WebjarTestTask {
    fun install(project: Project, webjar: WebjarExtension) {
        val testTask = project.tasks.register(WEBJAR_TEST_TASK, NpmTask::class.java) { task ->
            task.dependsOn(WEBJAR_INSTALL_TASK)
            task.group = WEBJAR_TASK_GROUP
            filterExistingDirs(project, webjar.srcDir, webjar.testDir).forEach {
                task.inputs.dir(it)
            }
            task.inputs.files(".babelrc", "package.json", "package-lock.json")
            task.outputs.file(project.buildDir.resolve(webjar.testTimestampFile))
            task.setArgs(listOf("run", webjar.testTaskName))
            task.doLast { createTimeMarkerFile(project, webjar.testTimestampFile) }
        }
        if (!WebjarSkipCondition.isWebjarSkipped(project)) {
            project.tasks.named(TEST_TASK_NAME).configure {
                it.dependsOn(testTask)
            }
        }
    }
}