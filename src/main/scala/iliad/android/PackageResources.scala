package iliad
package android

import java.util

import iliad.io.IOReader
import iliad.android.io.{BuilderReader, BuilderAPI}

import scala.collection.JavaConverters._

import com.android.builder.dependency.SymbolFileProvider

import sbt._
import Keys._

/** Packages resources using aapt (Android Asset Packaging Tool) */
object PackageResources {
  import layoutKeys._
  import androidKeys._


  private def runTask(log: Logger, builder: BuilderAPI, resources: File, assets: File,
                      manifest: File, packageForResources: String,
                      resourceApk: File, generatedSources: File): Unit = {

    val cmd = AaptCommand(resources, assets, packageForResources, manifest, resourceApk, generatedSources)
    val packageOperation = BuilderReader.processResources(log, cmd)
    val ioOperation = IOReader.createDirectory(generatedSources)

    log.info("Creating output directory for resource packaging")
    ioOperation.run(IO)
    log.info("Created output directory for resource packaging")

    log.info("Starting to package resources")
    packageOperation.run(builder)
    log.info("Finished packaging resources")
  }

  /** Packages resources using AAPT (Android Asset Packaging Tool)
    *
    * See [[http://elinux.org/Android_aapt]] for further details.
    * Uses resources from [[androidResources]].
    * Uses assets from [[assets]].
    * Outputs generated source R.java to [[generatedROut]].
    * Outputs resource apk to [[resourceApk]].
    */
  def apply() = Def.task(runTask(streams.value.log, androidBuilder.value,
    androidResources.value, assets.value, manifest.value, packageForResources.value,
    resourceApk.value, generatedROut.value))
}


import com.android.builder.model.AaptOptions
import com.android.builder.core.{VariantType, AaptPackageProcessBuilder}

/** Aapt options used in resource packaging.
  *
  * These are the only options currently supported by the plugin. */
private object DefaultAaptOptions extends AaptOptions {
  override def getNoCompress: util.Collection[String] = List.empty[String].asJava
  override def getFailOnMissingConfigEntry: Boolean = false
  override def getAdditionalParameters: util.List[String] = List.empty[String].asJava
  override def getIgnoreAssets: String = null
}

/** Immutable representation of [[com.android.builder.core.AaptPackageProcessBuilder]] */
private case class AaptCommand(resources: File, assets: File, packageForResources: String, manifest: File, resourceApk: File, generatesSources: File) {

  def command: AaptPackageProcessBuilder = {
    val aaptCommand: AaptPackageProcessBuilder = new AaptPackageProcessBuilder(manifest, DefaultAaptOptions)
    aaptCommand.setResFolder(resources)
    aaptCommand.setAssetsFolder(assets)
    aaptCommand.setPackageForR(packageForResources)
    aaptCommand.setResPackageOutput(resourceApk.absolutePath)
    aaptCommand.setType(VariantType.DEFAULT)
    aaptCommand.setDebuggable(true)
    aaptCommand.setSourceOutputDir(generatesSources.absolutePath)
    aaptCommand.setSymbolOutputDir(generatesSources.absolutePath)
    aaptCommand.setLibraries(List.empty[SymbolFileProvider].asJava)
    aaptCommand
  }
}

/** Extracts sources generated in resource packaging.
  *
  * Resource packaging should produce an R.java file containing references to resources.
  * This is a task in [[sourceGenerators]], so is executed before compilation.
  */
object GeneratedR {
  import layoutKeys._
  import taskKeys._

  private def runTask(log: Logger, generatedSources: File): Seq[File] = {
    val sources = (generatedSources ** "*.java").get

    log.info(s"generated ${sources.size} sources:")
    sources.foreach(f => log.info(f.absolutePath))

    sources
  }

  /** Extracts sources generated in resource packaging
    *
    * Returns all java sources in [[generatedROut]].
    */
  def apply() = Def.task {
    packageResources.value
    runTask(streams.value.log, generatedROut.value)
  }
}


