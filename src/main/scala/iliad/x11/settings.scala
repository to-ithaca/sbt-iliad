package iliad
package x11

import sbt._
import Keys._

trait AllSettings {
  import iliad.common.commonKeys._

  /** Configuration for all X11 tasks */
  val X11 = config("x11") extend Compile

  val settings = inConfig(X11)(Defaults.compileSettings ++ Seq(
    sourceDirectory := (sourceDirectory in Compile).value,
    generateApp := GenerateApp().value,
    sourceGenerators <+= generateApp,
    run <<= Defaults.runTask(fullClasspath, mainClass in run, runner in run),
    runMain <<= Defaults.runMainTask(fullClasspath, runner in run),
    mainClass := Some(s"${targetPackage.value}.${generatedAppName.value}"),
    targetOut := target.value / "x11",
    generatedAppOut := targetOut.value / "generated"
  )) ++ Seq(
    ivyConfigurations += X11,
    libraryDependencies += "com.ithaca" %% "iliad-kernel-x11" % "0.0.1-SNAPSHOT" % X11
  )
}


