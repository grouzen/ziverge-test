package com.ziverge.task

import com.ziverge.task.App.State
import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

case class WordsCountResponse(data: State)

object WordsCountResponse {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val wordsCountResponseCodec: Codec[WordsCountResponse] = deriveConfiguredCodec[WordsCountResponse]

}
