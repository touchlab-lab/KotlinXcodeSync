package co.touchlab.kotlinxcodesync

import org.gradle.api.Plugin
import org.gradle.api.Project

open class SyncPlugin : Plugin<Project> {
  /*companion object {
    internal val configName = "xcodeSource"
  }*/

  override fun apply(project: Project) {
    val extension = project.extensions.create("xcode", SyncExtension::class.java)

    /*project.configurations.create(configName) {
      it.setTransitive(false)
      it.setDescription("Source dependencies for Xcode sync")
    }*/

    project.afterEvaluate {
      project.tasks.register("xcodeSync", SyncTask::class.java) { task ->
        task.group = "xcode"
        task.description = "Sync Kotlin files with an Xcode project"
        task.config = extension
      }
    }
  }
}