package iliad
package android

import com.android.SdkConstants
import sbt.Keys._
import sbt._

import iliad.common.GLMode

trait AllSettings {
  import allKeys._
  import iliad.common.commonKeys._

  /** Configuration for all Android tasks */
  val Android = config("android")

  /** Configuration for Android instrumentation testing
    *
    * Should not be used normally - refer to android:test to execute instrumentation testing
    */
  val AndroidTest = config("androidTest")

  /** Common settings for Android and AndroidTest configurations */
  val commonSettings = Seq(
    deviceStream := Devices().value,
    aars := Aars().value,
    generateApp := GenerateApp().value,
    proguard := Proguard().value,
    dex := Dex().value,
    buildApk:= BuildApk().value,
    packageResources := PackageResources().value,
    generatedR := GeneratedR().value,
    install := Install().value,
    run:= Run().value,
    checkJars := CheckJars().value,
    glMode := GLMode.BASIC
  ) ++
    layoutSettings.settings ++
    androidSDKSettings.settings ++
    proguardSettings.settings

  val androidSettings = inConfig(Android)(Defaults.compileSettings ++ commonSettings ++ Seq(
    sourceDirectory := (sourceDirectory in Compile).value,
    unmanagedClasspath ++= aars.value :+ Attributed.blank(sdkJar.value),
    sourceGenerators <+= generateApp,
    sourceGenerators <+= generatedR,
    targetOut := target.value / "android",
    apkName := name.value,
    packageForResources := targetPackage.value,
    test := (test in AndroidTest).value
  ))

  val androidTestSettings = inConfig(AndroidTest)(Defaults.testSettings ++ commonSettings ++ Seq(
    sourceDirectory := sourceDirectory.value / "androidTest",
    unmanagedClasspath ++= aars.value :+ Attributed.blank(sdkJar.value),
    targetOut := target.value / "androidTest",
    apkName := name.value + "-androidTest",
    apkOut := targetOut.value / (apkName.value + ".ap_"),
    test := AndroidTestTask().value,
    packageForResources := testPackage.value
))
 
  /** All combined settings */
  val settings = androidSettings ++ androidTestSettings ++ Seq(
    ivyConfigurations += Android,
    libraryDependencies += "com.ithaca" %% "iliad-android" % "0.0.1-SNAPSHOT" % Android
  )
}

/** Android specific settings
  *
  * The ANDROID_HOME environment variable must be set.
  * The Android SDK and adb.exe are found through the ANDROID_HOME environment variable.
  */
trait AndroidSDKSettings {
  import androidKeys._
  import iliad.common.commonKeys._
  val settings = Seq(
    androidHome := androidHomeTask.value,
    adb := androidHome.value / (SdkConstants.OS_SDK_PLATFORM_TOOLS_FOLDER + SdkConstants.FN_ADB),
    sdkJar := androidHome.value / "platforms" / targetPlatform.value / "android.jar",
    supportRepository := androidHome.value / "extras" / "android" / "m2repository",
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
    exportJars := true,
    javaHomeDir := javaHomeTask.value,
    proguardLibraryJars := Seq(sdkJar.value, javaHomeDir.value),
    skipProguard := false,
    proguardInputClasspath := fullClasspath.value
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
  import iliad.common.commonKeys._
  val settings = Seq(
    proguardConfig := sourceDirectory.value / "proguard-project.txt",
    manifest := sourceDirectory.value / "AndroidManifest.xml",
    androidResources := sourceDirectory.value / "res",
    assets := sourceDirectory.value / "assets",

    aarOut := targetOut.value / "dependency-aars",

    generatedAppOut := targetOut.value / "generatedActivity",

    proguardOut := targetOut.value / "proguard",
    proguardJars := proguardOut.value / "jars",

    checkJarsOut := targetOut.value / "checkJars",

    dexOut := targetOut.value / "dex",

    resourceApk := targetOut.value / "resources.ap_",
    generatedROut := targetOut.value / "generatedSources",

    apkOut := targetOut.value / (apkName.value + ".apk")
  )
}
