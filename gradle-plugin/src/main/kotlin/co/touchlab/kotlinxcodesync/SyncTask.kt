package co.touchlab.kotlinxcodesync

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File

open class SyncTask : DefaultTask() {
  lateinit var config: SyncExtension

  @TaskAction
  fun syncProject() {
    copyRubyFile()

    val projectPath = config.projectPath
    val target = config.projectPath

    if(projectPath.isNullOrEmpty() || target.isNullOrEmpty()){
      throw IllegalArgumentException("projectPath and target required")
    }

    val std = ByteArrayOutputStream()
    val err = ByteArrayOutputStream()
    val result = projectExec(project,
      "ruby",
      null,
      mutableListOf(
        "build/projimport.rb",
        config.projectPath!!,
        config.target!!,
        config.group,
        File(project.projectDir, "src").path),
      std,
      err
    )

    logger.info(String(std.toByteArray()))
    if(result.exitValue != 0){
      logger.error(String(err.toByteArray()))
    }
  }

  fun copyRubyFile() {
    val whiteListFile = File(project.buildDir, "projimport.rb")
//    if (!whiteListFile.exists()) {
    val rbText = javaClass.getResource("/projimport.rb").readText()
    whiteListFile.writeText(rbText)
//    }
  }

}