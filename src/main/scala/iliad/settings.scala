package iliad

import com.android.SdkConstants
import sbt.Keys._
import sbt._

trait AllSettings {
  import allKeys._

  /** Configuration for all Android tasks */
  val Android = config("android") extend Compile

  /** Configuration for Android instrumentation testing
    *
    * Should not be used normally - refer to android:test to execute instrumentation testing
    */
  val AndroidTest = config("androidTest") extend Test

  /** Common settings for Android and AndroidTest configurations */
  val commonSettings = Seq(
    deviceStream := Devices().value,
    proguard := Proguard().value,
    dex := Dex().value,
    buildApk:= BuildApk().value,
    packageResources := PackageResources().value,
    generatedR := GeneratedR().value,
    install := Install().value,
    (unmanagedClasspath in Compile) := (unmanagedClasspath in Compile).value :+ Attributed.blank(sdkJar.value),
    (unmanagedClasspath in Test) := (unmanagedClasspath in Test).value :+ Attributed.blank(sdkJar.value)
  ) ++
    layoutSettings.settings ++
    androidSDKSettings.settings ++
    proguardSettings.settings

  val androidSettings = commonSettings ++ Seq(
    sourceDirectory := (sourceDirectory in Compile).value,
    proguardInputClasspath := (fullClasspath in Runtime).value,
    targetOut := target.value / "android",
    apkName := name.value,
    (test in Android) := (test in AndroidTest).value,
    packageForResources := targetPackage.value
  )

  val androidTestSettings = commonSettings ++ Defaults.testSettings ++ Seq(
    (exportJars in AndroidTest) := true,
    proguardInputClasspath := (fullClasspath in AndroidTest).value,
    targetOut := target.value / "androidTest",
    apkName := name.value + "-androidTest",
    apkOut := targetOut.value / (apkName.value + ".ap_"),
    test := AndroidTestTask().value,
    packageForResources := testPackage.value
  )

  /** All combined settings */
  val settings = inConfig(Android)(androidSettings) ++
    inConfig(AndroidTest)(androidTestSettings) ++ Seq(
    (dependencyClasspath in AndroidTest) := (dependencyClasspath in Test).value,
    (sourceDirectory in AndroidTest) := sourceDirectory.value / "androidTest",
    (sourceGenerators in Compile) <+= (generatedR in Android))
}

/** Android specific settings
  *
  * The ANDROID_HOME environment variable must be set.
  * The Android SDK and adb.exe are found through the ANDROID_HOME environment variable.
  */
trait AndroidSDKSettings {
  import androidKeys._
  val settings = Seq(
    androidHome := androidHomeTask.value,
    adb := androidHome.value / (SdkConstants.OS_SDK_PLATFORM_TOOLS_FOLDER + SdkConstants.FN_ADB),
    sdkJar := androidHome.value / "platforms" / targetPlatform.value / "android.jar",
    androidBuilder := Builder().value
  )

  private def androidHomeTask = Def.task(
    sys.env.get("ANDROID_HOME") match {
      case Some(h) => file(h)
      case None => sys.error("The ANDROID_HOME environment variable is not set.")
    })


}

/** Settings for proguard obfuscation
  *
  * Proguard requires jars to be exported on compile. It obfuscates these jars.
  * It requires the Java SDK and Android SDK as library jars - these must not be obfuscated by Proguard.
  */
trait ProguardSettings {
  import proguardKeys._
  import androidKeys._
  val settings = Seq(
    (exportJars in Compile) := true,
    (exportJars in Test) := true,
    javaHomeDir := javaHomeTask.value,
    proguardLibraryJars := Seq(sdkJar.value, javaHomeDir.value),
    skipProguard := false
  )

  private def javaHomeTask = Def.task(
    sys.props.get("java.home") match {
      case Some(h) => file(h)
      case None => sys.error("The java.home property is not set.")
    }
  )
}

/** Settings for the directory structure */
trait LayoutSettings {
  import layoutKeys._
  val settings = Seq(
    proguardConfig := sourceDirectory.value / "proguard-project.txt",
    manifest := sourceDirectory.value / "AndroidManifest.xml",
    androidResources := sourceDirectory.value / "res",
    assets := sourceDirectory.value / "assets",

    proguardOut := targetOut.value / "proguard",
    proguardJars := proguardOut.value / "jars",

    dexOut := targetOut.value / "dex",

    resourceApk := targetOut.value / "resources.ap_",
    generatedROut := targetOut.value / "generatedSources",

    apkOut := targetOut.value / (apkName.value + ".apk")
  )
}