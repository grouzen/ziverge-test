package com.ziverge.task

import io.circe.parser._
import cats.effect.Blocker
import cats.effect.ExitCode
import cats.effect.IO
import cats.syntax.traverse._
import cats.effect.IOApp
import fs2.io.stdinUtf8

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import cats.effect.concurrent.Ref
import scala.concurrent.duration._

object App extends IOApp {

  val stdinBlocker: Blocker =
    Blocker.liftExecutionContext(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool()))

  val WindowPeriod: FiniteDuration = 2.seconds

  override def run(args: List[String]): IO[ExitCode] =
    for {
      initTimestamp <- timer.clock.realTime(SECONDS)
      stateRef      <- Ref.of[IO, State](State(initTimestamp, Map.empty))

      httpServer = new HttpServer(stateRef)

      _ <- IO.race(httpServer.run, IO.race(countWords(stateRef), tick(stateRef)).void)
    } yield ExitCode.Success

  // This method adds a guarantee that if time is over for a current window the data will be wiped,
  // otherwise we will wait until new data will come.
  def tick(stateRef: Ref[IO, State]): IO[Unit] =
    fs2.Stream
      .awakeEvery[IO](WindowPeriod)
      .evalMap { _ =>
        for {
          currentTimestamp <- timer.clock.realTime(SECONDS)
          _                <- stateRef.update(resetData(_, currentTimestamp))
        } yield ()
      }
      .compile
      .drain

  def countWords(stateRef: Ref[IO, State]): IO[Unit] =
    stdinUtf8[IO](1024, stdinBlocker)
      .map { in =>
        in
          .split("\n")
          .toList
          .flatMap(decode[StreamMessage](_).toOption)
      }
      .evalMap(_.traverse(msg => updateState(stateRef, msg)).void)
      .compile
      .drain

  def updateState(stateRef: Ref[IO, State], msg: StreamMessage): IO[Unit] =
    for {
      latestTimestamp <- stateRef.get.map(_.latestTimestamp)

      _ <- if (msg.timestamp < latestTimestamp + WindowPeriod.toSeconds)
             stateRef.update(updateWordCount(_, msg))
           else
             stateRef.update { s =>
               updateWordCount(resetData(s, msg), msg)
             }
    } yield ()

  def updateWordCount(state: State, msg: StreamMessage): State = {
    val wordsByType = state.wordsCounts.getOrElse(msg.eventType, Map.empty)
    val count       = state.wordsCounts.get(msg.eventType).flatMap(_.get(msg.data)).getOrElse(0)

    state.copy(
      wordsCounts = state.wordsCounts.updated(msg.eventType, wordsByType + (msg.data -> (count + 1)))
    )
  }

  def resetData(state: State, msg: StreamMessage): State = {
    val reminder = msg.timestamp % WindowPeriod.toSeconds
    state.copy(
      latestTimestamp = state.latestTimestamp + (WindowPeriod.toSeconds - reminder),
      wordsCounts = Map.empty
    )
  }

  def resetData(state: State, currentTimestamp: Long): State =
    state.copy(latestTimestamp = currentTimestamp, wordsCounts = Map.empty)

}
