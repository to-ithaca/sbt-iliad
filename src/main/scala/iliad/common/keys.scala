package iliad

import sbt._
import Keys._

/** Keys used by all iliad configs */
trait CommonKeys {

  val generateApp: TaskKey[Seq[File]] = taskKey("Generates platform specific app")

  val appName: SettingKey[String] = settingKey("Trait corresponding to the application e.g. foo.bar.MyApp")
  val generatedAppName: SettingKey[String] = settingKey("Name of generated app e.g. MyGeneratedApp")
  val targetPackage: SettingKey[String] = settingKey("Target package for generated app")
  val generatedAppOut: SettingKey[File] = settingKey("Directory containing generated app")
  val width: SettingKey[Int] = settingKey("Width of the view in pixels")
  val height: SettingKey[Int] = settingKey("Height of the view in pixels")
  val targetOut: SettingKey[File] = settingKey("Target directory containing all output")

}



