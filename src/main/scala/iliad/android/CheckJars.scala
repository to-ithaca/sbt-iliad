package iliad
package android

import iliad.io.IOReader

import sbt._
import sbt.classpath.ClasspathUtilities
import Keys._

object CheckJars {
  import layoutKeys._
  import proguardKeys._

  private def runTask(log: Logger, classpath: Classpath, output: File): Unit = {
    val inputs = classpath.files.filter(ClasspathUtilities.isArchive)
    val pairs = inputs.map { i => 
      i -> output / i.name
    }

    log.info("Unzipping classpath jars")
    pairs.foreach {
      case (i, o) => IOReader.unzip(i, o).run(IO)
    }

    pairs.map { case (i, o) => 
      log.info(s"version of ${o.name} : ")
      val classFile = (o ** "*.class").get.head
      val command = s"javap -v ${classFile.absolutePath} | grep major"
      log.info(s"Performing: $command")
      s"javap -v ${classFile.absolutePath}" #| "grep major" ! log
    }
  }

  def apply() = Def.task {
    runTask(streams.value.log, 
      proguardInputClasspath.value,
      checkJarsOut.value
    )
  }

}
