package io.github.markehammons.latomate.gitlab_api.projects

import java.net.URI
import java.time.ZonedDateTime

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import io.circe.{Decoder, Encoder}

@ConfiguredJsonCodec
case class SimpleProject(id: Int,
                         description: Option[String],
                         defaultBranch: String,
                         sshUrlToRepo: String,
                         httpUrlToRepo: URI,
                         webUrl: URI,
                         readmeUrl: URI,
                         tagList: Seq[String],
                         name: String,
                         nameWithNamespace: String,
                         path: String,
                         createdAt: ZonedDateTime,
                         lastActivity_at: ZonedDateTime,
                         forksCount: Int,
                         avatarUrl: URI,
                         starCount: Int)

object SimpleProject {
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val encodeURI = Encoder.encodeString.contramap[URI](_.toString)

  implicit val decodeURI = Decoder.decodeString.map(new URI(_))
}
