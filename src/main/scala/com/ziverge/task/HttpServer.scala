package com.ziverge.task

import cats.effect.ConcurrentEffect
import cats.effect.IO
import cats.effect.Timer
import cats.effect.concurrent.Ref
import com.ziverge.task.App.State
import org.http4s._
import org.http4s.dsl.io._
import io.circe.syntax._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.global

class HttpServer(stateRef: Ref[IO, State]) {

  val wordsService = HttpRoutes.of[IO] {
    case GET -> Root / "words" =>
      stateRef.get.flatMap(s => Ok(WordsCountResponse(s).asJson))
  }

  val httpApp = Router("/" -> wordsService).orNotFound

  def run(implicit concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO]): IO[Unit] =
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain

}
