package iliad.io

import com.android.ide.common.process._
import sbt.{Fork, ForkOptions}

import scala.collection.JavaConverters._

/** Executes external java processes using sbt */
object SbtJavaProcessExecutor extends JavaProcessExecutor {
  import language.existentials
  override def execute(info: JavaProcessInfo, h: ProcessOutputHandler) = {
    val envVars = info.getEnvironment.asScala.mapValues(_.toString).toMap
    val runJVMOptions = info.getJvmArgs.asScala ++ ("-cp" :: info.getClasspath :: Nil)
    val config = ForkOptions(envVars = envVars, runJVMOptions = runJVMOptions)
    val args = (info.getMainClass :: Nil) ++ info.getArgs.asScala
    val exitCode = Fork.java(config, args)

    new ProcessResult {
      override def assertNormalExitValue() = {
        if (exitCode != 0) {
          val e = new ProcessException(
            s"Android SDK command ${info.getMainClass} failed with exit code ($exitCode)")
          e.setStackTrace(Array.empty)
          throw e
        }
        this
      }

      override def rethrowFailure() = this
      override def getExitValue = exitCode
    }
  }
}
