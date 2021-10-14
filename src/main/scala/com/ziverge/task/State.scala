package com.ziverge.task

case class State(
  latestTimestamp: Long,
  wordsCounts:     Map[String, Map[String, Int]])
