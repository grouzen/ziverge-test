import Deps._

val scala2Version = "2.13.6"

lazy val root = project
  .in(file("."))
  .settings(
    name := "ziverge-test",
    version := "0.1.0",
    scalaVersion := scala2Version,
    libraryDependencies ++= Seq(
      Streaming.fs2Core,
      Streaming.fs2IO,
      JSON.circeCore,
      JSON.circeGeneric,
      JSON.circeGenericExtras,
      JSON.circeParser,
      Testing.scalatest
    )
  )
