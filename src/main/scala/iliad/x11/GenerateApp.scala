package iliad
package x11

import sbt._
import Keys._

import iliad.common.CodeGenerator
import iliad.common.GLMode

/** Task for generating X11 runnable app */
object GenerateApp extends CodeGenerator {
  import iliad.common.commonKeys._

  private def template(width: Int, height: Int, targetPackage: String, generatedAppName: String, 
    appName: String, mode: GLMode): String = s"""
package $targetPackage

object $generatedAppName extends $appName {

   ${mode.runner}
   def width: Int = $width
   def height: Int = $height

   def main(args: Array[String]): Unit = {
     _root_.iliad.Session.start($width, $height)
   }
}
"""
  private def runTask(log: Logger, root: File, targetPackage: String, appName: String, generatedAppName: String, width: Int, height: Int, mode: GLMode): Seq[File] = {
    val code = template(width, height, targetPackage, generatedAppName, appName, mode)
    generateCode(log, root, targetPackage, generatedAppName, code)
  }


  /** Task for generating X11 runnable app
    * 
    * Generates app with name [[generatedAppName]] in package [[targetPackage]].
    * Generates app in directory [[generatedAppOut]].
    * App window has dimensions [[width]] and [[height]].
    */
  def apply() = Def.task {
    runTask(streams.value.log, generatedAppOut.value, targetPackage.value, appName.value, generatedAppName.value, width.value, height.value, glMode.value)   
  }
}
