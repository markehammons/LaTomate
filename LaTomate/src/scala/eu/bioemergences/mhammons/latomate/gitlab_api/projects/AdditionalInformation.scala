package eu.bioemergences.mhammons.latomate.gitlab_api.projects

case class AdditionalInformation(
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
    _links: Links
)
