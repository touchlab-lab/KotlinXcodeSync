package co.touchlab.kotlinxcodesync

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.file.CopySpec
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

    val dependencyConfig = project.configurations.getByName(SyncPlugin.configName)

    val dependencySources = dependencyConfig.resolvedConfiguration.resolvedArtifacts.map { ra : ResolvedArtifact ->
      val classifier = ra.classifier
      logger.warn("dep $ra / ${ra.classifier}")
      if (classifier == null || !classifier.equals("sources")) {
        throw IllegalArgumentException("xcodeSource dependencies must have a 'sources' classifier $ra / $classifier")
      } else {
        val sourceJarFile = ra.file

        val explodedPath : String? = if (sourceJarFile.name.endsWith(".jar")) {
          val group = ra.moduleVersion.id.group
          val name = ra.moduleVersion.id.name
          val version = ra.moduleVersion.id.version
          var foldername = group + "_" + name + "_" + version

          foldername = foldername.replace('-', '_')
          foldername = foldername.replace('.', '_')
          foldername = foldername.replace(' ', '_')

          val xcodeSourceDir = File(project.buildDir, "xcodeSource")
          val folderLocation = File(xcodeSourceDir, foldername)

          folderLocation.mkdirs()
          project.copy { cp: CopySpec ->
            cp.from(project.zipTree(sourceJarFile))
            cp.into(folderLocation)
          }
          folderLocation.path
        } else {
          null
        }

        explodedPath
      }
    }.filterNotNull()

    val scriptArgs = mutableListOf(
        "build/projimport.rb",
        config.projectPath!!,
        config.target!!,
        config.group,
        File(project.projectDir, "src").path)

    scriptArgs.addAll(dependencySources)

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
    val whiteListFile = File(project.buildDir, "projimport.rb")
//    if (!whiteListFile.exists()) {
    val rbText = javaClass.getResource("/projimport.rb").readText()
    whiteListFile.writeText(rbText)
//    }
  }

}