package com.ziverge.task

import io.circe.parser._
import cats.effect.Blocker
import cats.effect.ExitCode
import cats.effect.IO
import cats.syntax.traverse._
import cats.syntax.flatMap._
import cats.effect.IOApp
import fs2.io.stdinUtf8

import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import cats.effect.concurrent.Ref
import fs2.Chunk
import fs2.Pull

import scala.concurrent.duration._

object App extends IOApp {

  type State = Map[String, Map[String, Int]]

  implicit class StreamExt[F[_], O](stream: fs2.Stream[F, O]) {

    // Can't find any similar combinator in fs2, so decided to write my own
    def windowed(windowSize: Long)(extract: O => Long): fs2.Stream[F, Chunk[O]] = {
      def go(buffer: Chunk.Queue[O], s: fs2.Stream[F, O]): Pull[F, Chunk[O], Unit] =
        s.pull.uncons.flatMap {
          case Some(hd -> tl) =>
            val windowStart = buffer.toChunk.head match {
              case h @ Some(_) => h.map(extract)
              case _           => hd.head.map(extract)
            }

            windowStart match {
              case Some(ws) =>
                hd.indexWhere(o => extract(o) > ws + windowSize) match {
                  case Some(idx) =>
                    val pfx = hd.take(idx)
                    val b2  = buffer :+ pfx

                    Pull.output1(b2.toChunk) >>
                      go(Chunk.Queue.empty, tl.cons(hd.drop(idx)))
                  case _ =>
                    go(buffer :+ hd, tl)
                }
              case _ => go(buffer, tl)
            }
          case None =>
            if (buffer.nonEmpty) Pull.output1(buffer.toChunk)
            else Pull.done
        }

      go(Chunk.Queue.empty, stream).stream
    }

  }

  val stdinBlocker: Blocker =
    Blocker.liftExecutionContext(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool()))

  val WindowPeriod: FiniteDuration = 20.seconds

  override def run(args: List[String]): IO[ExitCode] =
    for {
      stateRef <- Ref.of[IO, State](Map.empty)
      httpServer = new HttpServer(stateRef)

      _ <- IO.race(httpServer.run, countWords(stateRef)).void
    } yield ExitCode.Success

  def countWords(stateRef: Ref[IO, State]): IO[Unit] =
    stdinUtf8[IO](1024, stdinBlocker)
      .map { in =>
        in
          .split("\n")
          .toList
          .flatMap(decode[StreamMessage](_).toOption)
      }
      .flatMap(fs2.Stream.emits)
      .windowed(WindowPeriod.toSeconds)(_.timestamp)
      .evalMap(chunk => updateState(stateRef, chunk.toList))
      .compile
      .drain

  def updateState(stateRef: Ref[IO, State], messages: List[StreamMessage]): IO[Unit] =
    stateRef.update(_ => Map.empty) >>
      messages.traverse(msg => stateRef.update(updateWordCount(_, msg))).void

  def updateWordCount(state: State, msg: StreamMessage): State = {
    val wordsByType = state.getOrElse(msg.eventType, Map.empty)
    val count       = state.get(msg.eventType).flatMap(_.get(msg.data)).getOrElse(0)

    state.updated(msg.eventType, wordsByType + (msg.data -> (count + 1)))
  }

}
