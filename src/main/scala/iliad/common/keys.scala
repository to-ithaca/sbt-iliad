package iliad

import sbt._
import Keys._

/** Keys used by all iliad configs */
trait CommonKeys {
  val appName: SettingKey[String] = settingKey("Trait corresponding to the application e.g. foo.bar.MyApp")
  val generatedAppName: SettingKey[String] = settingKey("Name of generated app e.g. MyGeneratedApp")
  val targetPackage: SettingKey[String] = settingKey("Target package for generated app")
}
