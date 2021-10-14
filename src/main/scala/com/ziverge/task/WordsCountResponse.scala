package com.ziverge.task

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

case class WordsCountResponse(data: Map[String, Map[String, Int]])

object WordsCountResponse {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val wordsCountResponseCodec: Codec[WordsCountResponse] = deriveConfiguredCodec[WordsCountResponse]

}
