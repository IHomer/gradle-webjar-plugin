package com.coditory.gradle.webjar

import com.coditory.gradle.webjar.ProjectFiles.filterExistingDirs
import com.coditory.gradle.webjar.ProjectFiles.filterExistingFiles
import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_BUILD_TASK
import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_INSTALL_TASK
import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_TASK_GROUP
import com.moowork.gradle.node.npm.NpmTask
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_TASK_NAME

internal object WebjarBuildTask {
    fun install(project: Project) {
        val buildTask = project.tasks.create(WEBJAR_BUILD_TASK, NpmTask::class.java) { task ->
            task.group = WEBJAR_TASK_GROUP
            task.dependsOn(WEBJAR_INSTALL_TASK)
            filterExistingDirs(project, "src").forEach {
                task.inputs.dir(it)
            }
            filterExistingFiles(project, ".babelrc", "package.json", "package-lock.json").forEach {
                task.inputs.files(it)
            }
            task.outputs.dir(project.buildDir.resolve("dist"))
            task.setArgs(listOf("run", "build"))
            task.doLast { copyToJarOutput(project) }
        }
        if (!WebjarSkipCondition.isWebjarSkipped(project)) {
            project.tasks.named(BUILD_TASK_NAME).configure {
                it.dependsOn(buildTask)
            }
        }
    }

    private fun copyToJarOutput(project: Project) {
        val from = project.projectDir.resolve("dist")
        if (from.isDirectory) {
            val to = project.buildDir.resolve("webjar/static")
            to.mkdirs()
            from.copyRecursively(to, true)
        }
    }
}
