lazy val publishSettings = Seq(
  name := "sbt-iliad",
  scalaVersion := "2.10.6",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  organization := "com.ithaca",
  version := "0.0.2-SNAPSHOT"
)

val tools = "2.1.0"

lazy val commonSettings = Seq(
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
  ),
  libraryDependencies ++= Seq(
    "com.android.tools.build" % "gradle" % tools exclude("net.sf.proguard", "proguard-gradle"),
    "com.android.tools.build" % "builder-model" % tools,
    "net.sf.proguard" % "proguard-gradle" % "5.3",
    "org.scalaz.stream" %% "scalaz-stream" % "0.8",
    "org.typelevel" %% "cats" % "0.6.0"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
)

lazy val root = (project in file("."))
  .settings(sbtPlugin := true)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)

