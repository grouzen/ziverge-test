import sbt._

object Deps {

  object Streaming {
    val fs2Core = Orgs.fs2 %% "fs2-core" % Versions.fs2
    val fs2IO   = Orgs.fs2 %% "fs2-io"   % Versions.fs2
  }

}

object Orgs {

  val fs2 = "co.fs2"

}

object Versions {

  val fs2 = "2.5.9"

}
