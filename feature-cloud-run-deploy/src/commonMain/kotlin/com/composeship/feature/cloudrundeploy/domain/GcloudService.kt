package com.composeship.feature.cloudrundeploy.domain

import com.composeship.core.domain.service.ProcessOutput
import kotlinx.coroutines.flow.Flow

interface GcloudService {
    fun deploy(
        projectRoot: String,
        gcloudProjectId: String,
        region: String,
        serviceName: String
    ): Flow<ProcessOutput>

    fun deployFromGitHub(
        githubRepoUrl: String,
        githubBranch: String,
        gcloudProjectId: String,
        region: String,
        serviceName: String
    ): Flow<ProcessOutput>

    fun getCurrentProjectId(): Flow<ProcessOutput>

    fun listServices(gcloudProjectId: String, region: String): Flow<ProcessOutput>
}
