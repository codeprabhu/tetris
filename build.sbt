ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "edu.extetris"
ThisBuild / version := "0.1.0-SNAPSHOT"

val scalaTestVersion = "3.2.18"
val scalaFXVersion = "21.0.0-R32"
val circeVersion = "0.14.7"

lazy val root = (project in file("."))
  .settings(
    name := "extetris-platform",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.scalafx" %% "scalafx" % scalaFXVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-literal" % circeVersion % Test
    ),
    Compile / run / fork := true,
    Compile / mainClass := Some("extetris.gui.MainApp"),
    assembly / mainClass := Some("extetris.gui.MainApp"),
    assembly / assemblyMergeStrategy := {
      case PathList("module-info.class") => MergeStrategy.discard
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    javacOptions ++= Seq("--release", "17"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials"
    )
  )