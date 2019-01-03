package io.github.markehammons.latomate.gitlab_api.projects

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

@ConfiguredJsonCodec
case class Namespace(
    id: Int,
    name: String,
    path: String,
    kind: String,
    fullPath: String
)

object Namespace {
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
