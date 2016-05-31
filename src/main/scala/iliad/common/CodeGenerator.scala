package iliad
package common

import iliad.io.IOReader

import sbt._

trait CodeGenerator {
 
  def generateCode(log: Logger, root: File, targetPackage: String, fileName: String, code: String): Seq[File] = {
    val parentDirs = targetPackage.split("\\.")
    val targetFile = parentDirs.foldLeft(root)((dir, name) => dir / name) / s"$fileName.scala"

    val ioOperation = IOReader.write(targetFile, code)

    log.info(s"Generating $fileName in ${targetFile.absolutePath}")
    log.info(s"Using package $targetPackage")

    ioOperation.run(IO)

    Seq(targetFile)
  }

}
