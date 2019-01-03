package io.github.markehammons.latomate.gitlab_api.projects

import java.time.ZonedDateTime

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

@ConfiguredJsonCodec
case class Owner(
    id: Int,
    name: String,
    createdAt: ZonedDateTime
)

object Owner {
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
