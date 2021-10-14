package com.ziverge.task

import io.circe.parser._
import cats.effect.Blocker
import cats.effect.Clock
import cats.effect.ExitCode
import cats.effect.IO
import cats.syntax.traverse._
import cats.syntax.flatMap._
import cats.effect.IOApp
import fs2.io.stdinUtf8

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import cats.effect.concurrent.Ref
import scala.concurrent.duration._

object App extends IOApp {

  val stdinBlocker: Blocker =
    Blocker.liftExecutionContext(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool()))

  val WindowPeriod: Long = 10L

  override def run(args: List[String]): IO[ExitCode] =
    for {
      initTimestamp <- timer.clock.realTime(SECONDS)
      stateRef      <- Ref.of[IO, State](State(initTimestamp, Map.empty))
      _             <- readFromStream(stateRef)
    } yield ExitCode.Success

  def readFromStream(stateRef: Ref[IO, State]): IO[Unit] =
    stdinUtf8[IO](1024, stdinBlocker)
      .map { in =>
        in
          .split("\n")
          .toList
          .flatMap(decode[StreamMessage](_).toOption)
      }
      .evalMap { messages =>
        IO.delay(println(s"Messages: $messages")) >>
          messages.traverse(msg => updateState(stateRef, msg)).void >>
          stateRef.get.flatMap(s => IO.delay(println(s"State: $s")))
      }
      .compile
      .drain

  def updateState(stateRef: Ref[IO, State], msg: StreamMessage): IO[Unit] =
    for {
      latestTimestamp <- stateRef.get.map(_.latestTimestamp)

      _ <- if (msg.timestamp < latestTimestamp + WindowPeriod)
             stateRef.update(updateWordCount(_, msg))
           else
             stateRef.update { s =>
               updateWordCount(updateLatestTimestamp(s), msg)
             }
    } yield ()

  def updateWordCount(state: State, msg: StreamMessage): State = {
    val wordsByType = state.wordsCounts.getOrElse(msg.eventType, Map.empty)
    val count       = state.wordsCounts.get(msg.eventType).flatMap(_.get(msg.data)).getOrElse(0)

    state.copy(
      wordsCounts = state.wordsCounts.updated(msg.eventType, wordsByType + (msg.data -> (count + 1)))
    )
  }

  def updateLatestTimestamp(state: State): State =
    state.copy(
      latestTimestamp = state.latestTimestamp + WindowPeriod,
      wordsCounts = Map.empty
    )

}
