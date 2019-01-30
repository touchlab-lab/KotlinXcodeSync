package co.touchlab.kotlinxcodesync

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception

fun projectExec(
  proj: Project,
  executable: String,
  workingDir: File?,
  args:List<String>,
  stdout: ByteArrayOutputStream,
  stderr:ByteArrayOutputStream): ExecResult {

  var execSpec:ExecSpec ? = null
  var execResult:ExecResult? = null
  var execSucceeded = false

  /*
  args "@${Utils.relativePath(project.projectDir, javaBatch)}"

                setStandardOutput stdout
                setErrorOutput stderr

                setWorkingDir project.projectDir
   */
  try {
    execResult = proj.exec {
      it.executable = executable
      if (workingDir != null)
        it.workingDir = workingDir
      it.args = args
      it.standardOutput = stdout
      it.errorOutput = stderr
    }
    execSucceeded = true
    /*if (matchRegexOutputsRequired) {
      if (!matchRegexOutputs(stdout, stderr, matchRegexOutputsRequired)) {
        // Exception thrown here to output command line
        throw new InvalidUserDataException(
          'Unable to find expected expected output in stdout or stderr\n' +
            'Failed Regex Match: ' + escapeSlashyString(matchRegexOutputsRequired))
      }
    }*/

  } catch (e:Exception) {  // NOSONAR
    // ExecException is most common, which indicates "non-zero exit"
    val exceptionMsg = projectExecLog(/*execSpec, */stdout, stderr, execSucceeded, e)
    throw InvalidUserDataException(exceptionMsg, e)
  }

//  log.debug(projectExecLog(execSpec, stdout, stderr, execSucceeded, null))

  return execResult
}

fun projectExecLog(
  /*execSpec:ExecSpec, */stdout: ByteArrayOutputStream, stderr:ByteArrayOutputStream ,
  execSucceeded:Boolean, exception:Exception?):String {
  // Add command line and stderr to make the error message more useful
  // Chain to the original ExecException for complete stack trace

  var msg = if (execSucceeded) {
      "Command Line Succeeded:\n"
    } else {
      "Command Line Failed:\n"
    }

  /*msg += execSpec.commandLine.join(" ") + '\n'

  // Working Directory appears to always be set
  if (execSpec.getWorkingDir() != null) {
    msg += "Working Dir:\n"
    msg += execSpec.getWorkingDir().absolutePath + '\n'
  }*/

  // Use 'Cause' instead of 'Caused by' to help distinguish from exceptions
  if (exception != null) {
    msg += "Cause:\n"
    msg += exception.toString() + '\n'
  }

  // Stdout and stderr
  msg += stdOutAndErrToLogString(stdout, stderr)
  return msg
}

fun stdOutAndErrToLogString(stdout:ByteArrayOutputStream , stderr:ByteArrayOutputStream ):String {
  return "Standard Output:\n" +
    stdout.toString() + '\n' +
    "Error Output:\n" +
    stderr.toString()
}
internal fun List<String>.join(separator:String = ","):String{
  val sb = StringBuilder()
  this.forEach {
    if(sb.isNotEmpty())
      sb.append(separator)
    sb.append(it)
  }
  return sb.toString()
}