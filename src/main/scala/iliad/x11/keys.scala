package iliad
package x11

import sbt._
import Keys._

trait TaskKeys {
  val generateApp: TaskKey[Seq[File]] = taskKey("Generates the X11 app")
}

/** Keys to define view properties */
trait ViewKeys {
  val width: SettingKey[Int] = settingKey("Width of the view in pixels")
  val height: SettingKey[Int] = settingKey("Height of the view in pixels")
}

trait LayoutKeys {
  val targetOut: SettingKey[File] = settingKey("Directory containing X11 targets")
  val generatedAppOut: SettingKey[File] = settingKey("File containing generated X11 app")
}
