package com.coditory.gradle.webjar

import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_BUILD_TASK
import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_INSTALL_TASK
import com.coditory.gradle.webjar.WebjarPlugin.Companion.WEBJAR_TASK_GROUP
import com.coditory.gradle.webjar.shared.ProjectFiles.filterExistingDirs
import com.moowork.gradle.node.npm.NpmTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.PROCESS_RESOURCES_TASK_NAME

internal object WebjarBuildTask {
    fun install(project: Project, webjar: WebjarExtension) {
        val buildTask = project.tasks.register(WEBJAR_BUILD_TASK, NpmTask::class.java) { task ->
            task.group = WEBJAR_TASK_GROUP
            task.dependsOn(WEBJAR_INSTALL_TASK)
            setupCaching(project, webjar, task)
            task.setArgs(listOf("run", webjar.buildTaskName))
            task.doLast { copyToJarOutput(project, webjar) }
        }
        if (!WebjarSkipCondition.isWebjarSkipped(project)) {
            project.tasks.named(PROCESS_RESOURCES_TASK_NAME).configure {
                it.dependsOn(buildTask)
            }
        }
    }

    private fun setupCaching(project: Project, webjar: WebjarExtension, task: NpmTask) {
        filterExistingDirs(project, webjar.resolveSrcDirs()).forEach {
            task.inputs.dir(it)
        }
        task.inputs.files(".babelrc", ".tsconfig.json", "package.json", "package-lock.json")
        task.outputs.dir(project.buildDir.resolve(webjar.distDir))
        task.outputs.cacheIf { shouldCache(project, webjar) }
    }

    private fun shouldCache(project: Project, webjar: WebjarExtension): Boolean {
        return project.buildDir.resolve(webjar.distDir).isDirectory &&
            filterExistingDirs(project, webjar.resolveSrcDirs()).isNotEmpty()
    }

    private fun copyToJarOutput(project: Project, webjar: WebjarExtension) {
        val from = project.projectDir.resolve(webjar.distDir)
        if (from.isDirectory) {
            val to = project.buildDir
                .resolve(webjar.outputDir)
                .resolve(webjar.webjarDir)
            to.mkdirs()
            from.copyRecursively(to, true)
        }
    }
}
