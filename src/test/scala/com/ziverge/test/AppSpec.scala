package com.ziverge.test

import com.ziverge.task.App
import com.ziverge.task.State
import com.ziverge.task.StreamMessage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AppSpec extends AnyFlatSpec with Matchers {

  "updateWordCount" should "update correctly" in {
    val state = State(
      latestTimestamp = 10,
      wordsCounts = Map.empty
    )
    val msg = StreamMessage(eventType = "foo", data = "word1", timestamp = 11)

    val updated = App.updateWordCount(state, msg)

    updated.wordsCounts shouldBe Map("foo" -> Map("word1" -> 1))
  }

}
