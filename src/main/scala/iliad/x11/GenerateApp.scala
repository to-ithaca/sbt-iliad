package iliad
package x11

import sbt._
import Keys._

import iliad.common.CodeGenerator

/** Task for generating X11 runnable app */
object GenerateApp extends CodeGenerator {
  import iliad.common.commonKeys._

  def template(width: Int, height: Int, targetPackage: String, generatedAppName: String, appName: String): String = s"""
package $targetPackage

object $generatedAppName extends $appName {
   def main(args: Array[String]): Unit = {
     _root_.iliad.Session.start($width, $height)
   }
}
"""
  private def runTask(log: Logger, root: File, targetPackage: String, appName: String, generatedAppName: String, width: Int, height: Int): Seq[File] = {
    val code = template(width, height, targetPackage, generatedAppName, appName)
    generateCode(log, root, targetPackage, generatedAppName, code)
  }


  /** Task for generating X11 runnable app
    * 
    * Generates app with name [[generatedAppName]] in package [[targetPackage]].
    * Generates app in directory [[generatedAppOut]].
    * App window has dimensions [[width]] and [[height]].
    */
  def apply() = Def.task {
    runTask(streams.value.log, generatedAppOut.value, targetPackage.value, appName.value, generatedAppName.value, width.value, height.value)   
  }
}
