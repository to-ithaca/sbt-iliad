package iliad
package x11

import sbt._
import Keys._

/** Keys to define view properties */
trait ViewKeys {
  val width: SettingKey[Int] = settingKey("Width of the view in pixels")
  val height: SettingKey[Int] = settingKey("Height of the view in pixels")
}

/** Keys to define target layout */
trait LayoutKeys {
  val targetOut: SettingKey[File] = settingKey("Directory containing X11 targets")
}
