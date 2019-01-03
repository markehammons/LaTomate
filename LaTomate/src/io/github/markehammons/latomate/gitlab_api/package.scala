package io.github.markehammons.latomate

import java.net.URI

import io.circe.{Decoder, Encoder}

package object gitlab_api {
  implicit val encodeURI = Encoder.encodeString.contramap[URI](_.toString)

  implicit val decodeURI = Decoder.decodeString.map(new URI(_))

}
