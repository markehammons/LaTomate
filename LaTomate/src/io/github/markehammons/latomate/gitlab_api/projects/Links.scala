package io.github.markehammons.latomate.gitlab_api.projects

import java.net.URI

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import io.github.markehammons.latomate.gitlab_api._

@ConfiguredJsonCodec
case class Links(self: URI,
                 issues: URI,
                 mergeRequests: URI,
                 repoBranches: URI,
                 labels: URI,
                 events: URI,
                 members: URI)

object Links {
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames

}
