package com.ziverge.task

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = IO.pure(ExitCode.Success)

}
