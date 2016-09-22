package iliad
package android

import iliad.io.IOReader
import iliad.common.CodeGenerator
import iliad.common.GLMode

import sbt._
import Keys._

/** Generates the android.app.Activity */
object GenerateApp extends CodeGenerator {
  import layoutKeys._
  import iliad.common.commonKeys._
  import androidKeys._

  private def template(packageName: String, appName: String, activityName: String, mode: GLMode): String = s"""
package $packageName

final class $activityName extends _root_.iliad.AndroidBootstrap with $appName {
   def mainXML: Int = R.layout.activity_main
   def fragmentXML: Int = R.layout.fragment_main
   def subView: Int = R.id.gl_view
   def subFragment: Int = R.id.gl_fragment

   ${mode.runner}
}
"""

  private def runTask(log: Logger, root: File, targetPackage: String, appName: String, activityName: String, mode: GLMode): Seq[File] = {
    val code = template(targetPackage, appName, activityName, mode)
    generateCode(log, root, targetPackage, activityName, code)
  }

  /** Generates the android.app.Activity
    * 
    * Generates an activity from the app specified by [[appName]]
    * The generated activity is called [[generatedAppName]]
    * The generated activity is put in package [[targetPackage]]
    * The generated activity is written to [[generatedAppOut]]
    */
  def apply() = Def.task {
    runTask(streams.value.log, generatedAppOut.value, targetPackage.value, appName.value, generatedAppName.value, 
      glMode.value)
  }
}
