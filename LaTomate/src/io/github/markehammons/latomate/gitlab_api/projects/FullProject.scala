package io.github.markehammons.latomate.gitlab_api.projects

import java.net.URI
import java.time.ZonedDateTime

import io.github.markehammons.latomate.gitlab_api._
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec, JsonKey}

@ConfiguredJsonCodec
case class FullProject(
    id: Int,
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
    starCount: Int,
    visibility: String,
    owner: Owner,
    issuesEnabled: Boolean,
    openIssuesCount: Int,
    mergeRequestsEnabled: Boolean,
    jobsEnabled: Boolean,
    wikiEnabled: Boolean,
    snippetsEnabled: Boolean,
    resolveOutdatedDiffDiscussions: Boolean,
    containerRegistryEnabled: Boolean,
    creatorId: Int,
    namespace: Namespace,
    importStatus: String,
    archived: Boolean,
    sharedRunnersEnabled: Boolean,
    runnersToken: String,
    publicJobs: Boolean,
    sharedWithGroups: Seq[Int],
    onlyAllowMergeIfPipelineSucceeds: Boolean,
    onlyAllowMergeIfAllDiscussionsAreResolved: Boolean,
    requestAccessEnabled: Boolean,
    mergeMethod: String,
    statistics: Statistics,
    @JsonKey("_links") links: Links
)

object FullProject {
  implicit val config: Configuration =
    Configuration.default.withSnakeCaseMemberNames
}
