package iliad
package android
package io

import scala.collection.JavaConverters._

import com.android.build.gradle.internal.dsl.DexOptions
import com.android.builder.core.AndroidBuilder

import iliad.android.{AaptCommand, DebugSigningConfig}
import iliad.android.std.logger._

import sbt._

/** Simplified API for [[com.android.builder.core.AndroidBuilder]] */
final class BuilderAPI(val builder: AndroidBuilder) {

  /** Processes resources using AAPT (Android Asset Packaging Tool) */
  def processResources(log: Logger, cmd: AaptCommand) = {
    val enforceUniquePackageName = false
    builder.processResources(cmd.command, enforceUniquePackageName, log.outputHandler)
  }

  /** Dexes inputs
    *
    * Converts java byte code (JARs) into a dex file (Dalvik executable)
    */
  def convertByteCode(log: Logger, inputs: Seq[File], outputFolder: File) = {
    val multidex = false
    val mainDexList: File = null

    val dexOptions: DexOptions = new DexOptions
    dexOptions.setIncremental(false)
    dexOptions.setJavaMaxHeapSize("1024m")
    dexOptions.setJumboMode(false)
    dexOptions.setMaxProcessCount(java.lang.Runtime.getRuntime.availableProcessors)
    dexOptions.setThreadCount(java.lang.Runtime.getRuntime.availableProcessors)
    dexOptions.setPreDexLibraries(false)
    dexOptions.setDexInProcess(false)

    val additionalParameters: java.util.List[String] = List.empty[String].asJava
    val incremental = false
    val optimise = true
    val outputHandler = log.outputHandler

    builder.convertByteCode(inputs.asJava, outputFolder, multidex, mainDexList, dexOptions, additionalParameters, incremental, optimise, outputHandler)
  }


  /** Builds an apk */
  def packageApk(log: Logger, resourceApk: File, dex: File, output: File, minSdkVersion: Int, javaResourceLocations: Seq[File]) = {
    val resourceApkLocation: String = resourceApk.absolutePath
    val dexFolders: Set[File] = Set(dex)
    val jniLibsLocations: Seq[File] = Seq.empty
    val abiFilters: Set[String] = Set.empty
    val jniDebugBuild: Boolean = true
    val signingConfig = new DebugSigningConfig(log)
    val outputApkLocation: String = output.absolutePath

    builder.packageApk(resourceApkLocation,
      dexFolders.asJava,
      javaResourceLocations.asJava,
      jniLibsLocations.asJava,
      abiFilters.asJava, jniDebugBuild,
      signingConfig,
      outputApkLocation,
      minSdkVersion)
  }



}

import cats.data.Reader

/** Reader Monad for [[BuilderAPI]] */
object BuilderReader {
  def convertByteCode(log: Logger, inputs: Seq[File], outputFolder: File): Reader[BuilderAPI, Unit] =
    Reader(_.convertByteCode(log, inputs, outputFolder))

  def processResources(log: Logger, cmd: AaptCommand): Reader[BuilderAPI, Unit] =
    Reader(_.processResources(log, cmd))

  def packageApk(log: Logger, resourceApk: File, dex: File, output: File, minSdkVersion: Int, javaResourceLocations: Seq[File]): Reader[BuilderAPI, Unit] =
    Reader(_.packageApk(log, resourceApk, dex, output, minSdkVersion, javaResourceLocations))
}
