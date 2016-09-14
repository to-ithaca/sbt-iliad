package iliad
package x11

import sbt._
import Keys._

import iliad.common.GLMode

trait AllSettings {
  import iliad.common.commonKeys._

  /** Configuration for all X11 tasks */
  val X11 = config("x11")
  val X11Test = config("x11Test")

  val testSettings = inConfig(X11Test)(Defaults.testSettings ++ Defaults.testTasks ++
    Seq(
    sourceDirectory := (sourceDirectory in Test).value,
      unmanagedClasspath ++= (exportedProducts in compile in X11).value
    )) ++ Seq(
    ivyConfigurations += X11Test,
    libraryDependencies ++= {
      "com.ithaca" %% "iliad-x11" % "0.0.1-SNAPSHOT" % X11Test
      val modules = (libraryDependencies in Test).value
      val scoped = modules.map(_.copy(configurations = Some(X11Test.name)))
      scoped :+ "com.ithaca" %% "iliad-x11" % "0.0.1-SNAPSHOT" % X11Test
    }
  )
 

  val settings = inConfig(X11)(Defaults.compileSettings ++
    Seq(
    sourceDirectory := (sourceDirectory in Compile).value,
    generateApp := GenerateApp().value,
    sourceGenerators <+= generateApp,
    run <<= Defaults.runTask(fullClasspath, mainClass in run, runner in run),
    runMain <<= Defaults.runMainTask(fullClasspath, runner in run),
    mainClass := Some(s"${targetPackage.value}.${generatedAppName.value}"),
    test := (test in X11Test).value,
    targetOut := target.value / "x11",
    generatedAppOut := targetOut.value / "generated",
    glMode := GLMode.BASIC
  )) ++ Seq(
    ivyConfigurations += X11,
    libraryDependencies += "com.ithaca" %% "iliad-x11" % "0.0.1-SNAPSHOT" % X11
  ) ++ testSettings


}


