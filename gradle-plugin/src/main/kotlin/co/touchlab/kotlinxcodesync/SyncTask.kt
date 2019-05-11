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

    /*val ktExt = project.extensions.findByType(KotlinProjectExtension::class.java)
    val mpExt = project.extensions.findByType(KotlinMultiplatformExtension::class.java)

    mpExt?.let {
      it.
    }
    mpExt?.sourceSets?.let { ssList ->
      ssList.forEach { ss ->
        ss.dependencies()
      }
    }*/

    val projectPath = config.projectPath
    val target = config.projectPath

    if(projectPath.isNullOrEmpty() || target.isNullOrEmpty()){
      throw IllegalArgumentException("projectPath and target required")
    }

    val scriptArgs = mutableListOf(
        "build/projimport.rb",
        config.projectPath!!,
        config.target!!,
        config.group,
        File(project.projectDir, "src").path)

    val std = ByteArrayOutputStream()
    val err = ByteArrayOutputStream()
    val result = projectExec(project,
        "ruby",
        null,
        scriptArgs,
        std,
        err
    )

    logger.info(String(std.toByteArray()))
    if(result.exitValue != 0){
      logger.error(String(err.toByteArray()))
    }
  }

  fun copyRubyFile() {
    val rbFile = File(project.buildDir, "projimport.rb")
//    if (!rbFile.exists()) {
    val rbText = javaClass.getResource("/projimport.rb").readText()
    rbFile.writeText(rbText)
//    }
  }

}