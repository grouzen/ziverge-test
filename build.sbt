import Deps._

val scala2Version = "2.13.6"

lazy val root = project
  .in(file("."))
  .settings(
    name := "revolut-interview-scala",
    version := "0.1.0",
    scalaVersion := scala2Version,
    libraryDependencies ++= Seq(
      Streaming.fs2Core,
      Streaming.fs2IO
    )
  )
