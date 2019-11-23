package co.touchlab.kotlinxcodesync

import org.gradle.api.Plugin
import org.gradle.api.Project

open class SyncPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("xcodeSync", SyncExtension::class.java)

    project.afterEvaluate {
      project.tasks.register("xcodeSync", SyncTask::class.java) { task ->
        task.group = "xcode"
        task.description = "Sync Kotlin files with an Xcode project"
        task.config = extension
      }
    }
  }
}