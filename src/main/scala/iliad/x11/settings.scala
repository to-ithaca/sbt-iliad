package iliad
package x11

import sbt._
import Keys._

trait AllSettings {
  import allKeys._
  import iliad.common.commonKeys._

  /** Configuration for all X11 tasks */
  val X11 = config("x11")

  val settings = inConfig(X11)(Defaults.compileSettings ++ layoutSettings.settings ++ Seq(
    sourceDirectory := (sourceDirectory in Compile).value,
    generateApp := GenerateApp().value,
    sourceGenerators <+= generateApp,
    run <<= Defaults.runTask(fullClasspath, mainClass in run, runner in run),
    runMain <<= Defaults.runMainTask(fullClasspath, runner in run),
    mainClass := Some(s"${targetPackage.value}.${generatedAppName.value}")
  )) ++ Seq(
    libraryDependencies += "com.ithaca" %% "iliad-kernel-x11" % "0.0.1-SNAPSHOT"
  )
}

trait LayoutSettings {
  import layoutKeys._

  val settings = Seq (
    targetOut := target.value / "x11",
    generatedAppOut := targetOut.value / "generated"
  )
}
