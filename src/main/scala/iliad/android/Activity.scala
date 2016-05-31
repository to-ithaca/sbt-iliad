package iliad
package android

import iliad.io.IOReader
import iliad.common.CodeGenerator

import sbt._
import Keys._

object Activity extends CodeGenerator {
  import layoutKeys._
  import iliad.common.commonKeys._
  import androidKeys._

  private def template(packageName: String, appName: String, activityName: String): String = s"""
package $packageName

import iliad._
import iliad.android._
import iliad.kernel._
import iliad.implicits._

final class $activityName extends IliadBootstrap with $appName {
   def view: Int = R.layout.activity_main
}
"""

  private def runTask(log: Logger, root: File, targetPackage: String, appName: String, activityName: String): Seq[File] = {
    val code = template(targetPackage, appName, activityName)
    generateCode(log, root, targetPackage, activityName, code)
  }

  def apply() = Def.task {
    runTask(streams.value.log, activityOut.value, targetPackage.value, appName.value, generatedAppName.value)   
  }
}
