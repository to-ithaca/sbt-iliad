package iliad
package win32

import sbt._
import Keys._

trait AllSettings {
  import iliad.common.commonKeys._

  /** configuration for all win32 tasks */
  val Win32 = config("win32") extend Compile

  val settings = inConfig(Win32)(Defaults.compileSettings ++ Seq(
    sourceDirectory := (sourceDirectory in Compile).value,
    generateApp := GenerateApp().value,
    sourceGenerators <+= generateApp,
    run <<= Defaults.runTask(fullClasspath, mainClass in run, runner in run),
    runMain <<= Defaults.runMainTask(fullClasspath, runner in run),
    mainClass := Some(s"${targetPackage.value}.${generatedAppName.value}"),
    targetOut := target.value / "win32",
    generatedAppOut := targetOut.value / "generated"
  )) ++ Seq(
    ivyConfigurations += Win32,
    libraryDependencies += "com.ithaca" %% "iliad-kernel-win32" % "0.0.1-SNAPSHOT" % Win32
  )
}

