package com.ziverge.task

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

case class StreamMessage(
  eventType: String,
  data:      String,
  timestamp: Long)

object StreamMessage {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val messageCodec: Codec[StreamMessage] = deriveConfiguredCodec[StreamMessage]

}
