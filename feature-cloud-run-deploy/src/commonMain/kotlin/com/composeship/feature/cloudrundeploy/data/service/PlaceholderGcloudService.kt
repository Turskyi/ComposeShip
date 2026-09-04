package com.composeship.feature.cloudrundeploy.data.service

import com.composeship.core.domain.service.ProcessOutput
import com.composeship.feature.cloudrundeploy.domain.GcloudService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlaceholderGcloudService : GcloudService {
    override fun deploy(
        projectRoot: String,
        gcloudProjectId: String,
        region: String,
        serviceName: String
    ): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Stdout("Deployment from local path is not supported on this platform."))
        emit(ProcessOutput.Complete(1))
    }

    override fun deployFromGitHub(
        githubRepoUrl: String,
        githubBranch: String,
        gcloudProjectId: String,
        region: String,
        serviceName: String
    ): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Stdout("GitHub deployment is coming soon for Mobile/Web! Currently it requires the gcloud CLI (Desktop)."))
        emit(ProcessOutput.Complete(1))
    }

    override fun getCurrentProjectId(): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Complete(1))
    }

    override fun listServices(gcloudProjectId: String, region: String): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Complete(1))
    }
}
