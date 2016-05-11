package iliad

import iliad.io.{BuilderAPI, IOReader, BuilderReader}
import sbt._
import Keys._

/** Performs Android Dexing.
  *
  * See [[https://source.android.com/devices/tech/dalvik/dex-format.html]] for further details.
  */
object Dex {
  import layoutKeys._
  import taskKeys._
  import androidKeys._

  private def runTask(log: Logger, builder: BuilderAPI, inputDirectory: File, outputDirectory: File) = {
    val inputs = (inputDirectory ** "*").get
    val ioOperation = IOReader.createDirectory(outputDirectory)
    val dexOperation = BuilderReader.convertByteCode(log, inputs, outputDirectory)

    inputs.foreach(i => log.info(s"Dex input jar: ${i.absolutePath}"))
    log.info(s"Dex output is located at ${outputDirectory.absolutePath}")

    log.debug("Creating output directory")
    ioOperation.run(IO)
    log.debug("Created output directory")

    log.info("Starting dex")
    dexOperation.run(builder)
    log.info("Finished dex")
  }

  /** Performs dexing.
    *
    * Runs after proguard obfuscation.
    * Processes jars in [[proguardJars]].
    * Outputs a classes.dex file in [[dexOut]].
    */
  def apply() = Def.task {
    proguard.value
    runTask(streams.value.log, androidBuilder.value, proguardJars.value, dexOut.value)
  }
}