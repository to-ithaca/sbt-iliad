package iliad

import iliad.io.IOReader

import sbt._
import Keys._

object Activity {
  import layoutKeys._
  import activityKeys._
  import androidKeys._


  private def template(packageName: String, appName: String, activityName: String): String = s"""
package $packageName

import iliad._
import iliad.android._
import iliad.kernel._
import iliad.implicits._

final class $activityName extends IliadActivity with $appName {
   def view: Int = R.layout.activity_main
}
"""

  private def runTask(log: Logger, root: File, targetPackage: String, appName: String, activityName: String): Seq[File] = {
    val parentDirs = targetPackage.split("\\.")
    val targetFile = parentDirs.foldLeft(root)((dir, name) => dir / name) / s"$activityName.scala"
    val code = template(targetPackage, appName, activityName)

    val ioOperation = IOReader.write(targetFile, code)

    log.info(s"Generating $activityName in ${targetFile.absolutePath}")
    log.info(s"Using package $targetPackage")
    log.info(s"Using app $appName")

    ioOperation.run(IO)

    Seq(targetFile)
  }

  def apply() = Def.task {
    runTask(streams.value.log, activityOut.value, targetPackage.value, appName.value, activityName.value)   
  }
}
