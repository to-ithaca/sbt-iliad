package iliad
package win32

import sbt._
import Keys._

import iliad.common.CodeGenerator

/** generates win32 main */
object GenerateApp extends CodeGenerator {
  import iliad.common.commonKeys._

  def template(width: Int, height: Int, targetPackage: String, generatedAppName: String, appName: String): String = 
    s"""package $targetPackage
        object $generatedAppName extends _root_.iliad.kernel.Win32Bootstrap($appName, $width, $height) with $appName"""

  private def runTask(log: Logger, root: File, targetPackage: String, appName: String, generatedAppName: String, width: Int, height: Int): Seq[File] = {
    val code = template(width, height, targetPackage, generatedAppName, appName)
    generateCode(log, root, targetPackage, generatedAppName, code)
  }

  def apply() = Def.task {
    runTask(streams.value.log, generatedAppOut.value, targetPackage.value, appName.value, generatedAppName.value, width.value, height.value)   
  }
}
