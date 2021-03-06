import sbt._

object Deps {

  object Streaming {
    val fs2Core = Orgs.fs2 %% "fs2-core" % Versions.fs2
    val fs2IO   = Orgs.fs2 %% "fs2-io"   % Versions.fs2
  }

  object JSON {
    val circeCore          = Orgs.circe %% "circe-core"           % Versions.circe
    val circeGeneric       = Orgs.circe %% "circe-generic"        % Versions.circe
    val circeGenericExtras = Orgs.circe %% "circe-generic-extras" % Versions.circe
    val circeParser        = Orgs.circe %% "circe-parser"         % Versions.circe
  }

  object Testing {
    val scalastic = "org.scalactic" %% "scalactic" % "3.2.9"
    val scalatest = "org.scalatest" %% "scalatest" % "3.2.9" % "test"
  }

  object Http {
    val http4sDSL         = Orgs.http4s %% "http4s-dsl"          % Versions.http4s
    val http4sBlazeServer = Orgs.http4s %% "http4s-blaze-server" % Versions.http4s
    val http4sCirce       = Orgs.http4s %% "http4s-circe"        % Versions.http4s
  }

}

object Orgs {

  val fs2    = "co.fs2"
  val circe  = "io.circe"
  val http4s = "org.http4s"

}

object Versions {

  val fs2    = "2.5.9"
  val circe  = "0.14.1"
  val http4s = "0.21.30"

}
