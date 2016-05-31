package iliad
package x11

import sbt._
import Keys._

import iliad.common.CodeGenerator

/** Task for generating X11 runnable app */
object GenerateApp extends CodeGenerator {
  import iliad.common.commonKeys._
  import iliad.x11.viewKeys._
  import iliad.x11.layoutKeys._

  def template(width: Int, height: Int, targetPackage: String, generatedAppName: String, appName: String): String = s"""
package $targetPackage

import iliad._
import iliad.kernel._
import iliad.x11._

object $generatedAppName extends IliadBootstrap with $appName {
   val width: Int = $width
   val height: Int = $height
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
