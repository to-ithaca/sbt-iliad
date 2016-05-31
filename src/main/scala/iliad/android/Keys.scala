package iliad
package android

import com.android.ddmlib.IDevice
import iliad.android.io.BuilderAPI
import scalaz.concurrent.{Task => SZTask}

import sbt._
import Keys._

/** Keys for notable sbt tasks */
trait TaskKeys {
  val deviceStream: TaskKey[SZTask[List[IDevice]]] = taskKey("Scalaz stream of devices")

  val aars = taskKey[Seq[Attributed[File]]]("Unzips aar dependencies into jars")
  val proguard = taskKey[Unit]("Runs proguard obfuscation")
  val dex = taskKey[Unit]("Runs dex")
  val packageResources = taskKey[Unit]("Packages resources using AAPT (Android Asset Packaging Tool)")
  val generatedR = taskKey[Seq[File]]("List of sources generated from resource packaging")
  val buildApk = taskKey[Unit]("Builds apk")
  val install = taskKey[Unit]("Installs apk onto connected Android devices")
}

/** Keys for android variables */
trait AndroidKeys {
  val targetPlatform: SettingKey[String] = settingKey("Target android platform e.g. android-22")
  val minSdkVersion: SettingKey[Int] = settingKey("Minimum Android SDK version")

  val packageForResources: SettingKey[String] = settingKey("Target package for resource packaging")

  val androidHome: TaskKey[File] = taskKey("Location of ANDROID_HOME environment variable")
  val adb: TaskKey[File] = taskKey("Location of adb.exe")
  val sdkJar: TaskKey[File] = taskKey("Location of Android SDK jar")
  val supportRepository: TaskKey[File] = taskKey("Location of Android Support Repository")
  val supportAars: TaskKey[Seq[String]] = taskKey("Location of aar files relative to the support repository")
  val androidBuilder: TaskKey[BuilderAPI] = taskKey("Android Builder")
}

/** Keys for proguard obfuscation */
trait ProguardKeys {
  val proguardInputClasspath: TaskKey[Classpath] = taskKey("Input jars for proguard obfuscation")
  val proguardLibraryJars: TaskKey[Seq[File]] = taskKey("Library jars for proguard obfuscation")
  val javaHomeDir: TaskKey[File] = taskKey("Location of java.home system property")
  val skipProguard: SettingKey[Boolean] = settingKey("If true, proguard obfuscation is skipped")
}

/** Keys for directory structure */
trait LayoutKeys {
  /** Provided files and folders */
  val proguardConfig: SettingKey[File] = settingKey("File containing proguard config")
  val manifest: SettingKey[File] = settingKey("File containing Android manifest")
  val androidResources: SettingKey[File] = settingKey("Directory containing Android resources")
  val assets: SettingKey[File] = settingKey("Directory containing Android assets")

  /** Generated files and folders */
  val targetOut: SettingKey[File] = settingKey("Target directory containing all output")

  val aarOut: SettingKey[File] = settingKey("Directory containing unzipped aars")

  val proguardOut: SettingKey[File] = settingKey("Directory containing proguard output")
  val proguardJars: SettingKey[File] = settingKey("Directory containing proguard obfuscated jars")

  val dexOut: SettingKey[File] = settingKey("Directory containing dexed jars")

  val resourceApk: SettingKey[File] = settingKey("File containing the resources apk")
  val generatedROut: SettingKey[File] = settingKey("Directory containing R.java and other generated sources")

  val apkOut: SettingKey[File] = settingKey("File containing the final apk")

  val apkName: SettingKey[String] = settingKey("Name of the apk")
}

/** Keys for instrumentation testing */
trait TestKeys {
  val testPackage: SettingKey[String] = settingKey("Package containing tests")
}
