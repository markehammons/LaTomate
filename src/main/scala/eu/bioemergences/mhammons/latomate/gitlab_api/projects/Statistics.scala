package eu.bioemergences.mhammons.latomate.gitlab_api.projects

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

@ConfiguredJsonCodec
case class Statistics(commitCount: Int,
                      storageSize: Long,
                      repositorySize: Long,
                      lfsObjectsSize: Long,
                      jobArtifactsSize: Long)

object Statistics {
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
