package iliad
package android

import iliad.android.io.{BuilderReader, BuilderAPI}

import com.android.builder.model.SigningConfig
import com.android.builder.signing.DefaultSigningConfig
import com.android.ide.common.signing.KeystoreHelper

import sbt._
import Keys._

import iliad.android.std.logger._

/** Builds an apk */
object BuildApk {
  import androidKeys._
  import layoutKeys._
  import taskKeys._

  private def runTask(log: Logger, builder: BuilderAPI, javaResourceDirectory: File, resourceApk: File, dex: File,
                      apkOutput: File, minSdk: Int) = {
    val resources = (javaResourceDirectory ** "*.*").get
    val apkOperation = BuilderReader.packageApk(log, resourceApk, dex, apkOutput, minSdk, resources)

    log.info(s"Found ${resources.size} java resources: ")
    log.info(s"Writing apk to ${apkOutput.absolutePath}")
    apkOperation.run(builder)
    log.info("Finished packaging apk")
  }


  /** Builds an apk
    *
    * Runs after [[dex]].
    * Uses classes.dex in [[dexOut]].
    * Uses resource apk [[resourceApk]].
    * Collectes java resources (any files marked as resources) from all [[proguardJars]].
    * Outputs apk to [[apkOut]].
    */
  def apply() = Def.task {
    dex.value
    packageResources.value
    runTask(streams.value.log, androidBuilder.value,
      proguardJars.value, resourceApk.value, dexOut.value,
      apkOut.value, minSdkVersion.value)
  }
}

/** Default signing config
  *
  * This is the only config supported by the plugin.
  */
private final class DebugSigningConfig(val log: Logger) extends SigningConfig {

  override def getName: String = "debug"
  override def getKeyAlias: String = DefaultSigningConfig.DEFAULT_ALIAS
  override def isSigningReady: Boolean = true
  override def getStoreType: String = "jks"
  override def getStorePassword: String = DefaultSigningConfig.DEFAULT_PASSWORD
  override def getKeyPassword: String = getStorePassword
  override def getStoreFile: File = file(KeystoreHelper.defaultDebugKeystoreLocation)

  if (!getStoreFile.exists) {
    KeystoreHelper.createDebugStore(getStoreType, getStoreFile, getStorePassword,
      getKeyPassword, getKeyAlias, log.android)
  }
}
