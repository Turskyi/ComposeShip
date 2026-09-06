package com.composeship.feature.firebasedeploy.data.service

import com.composeship.core.domain.service.ProcessOutput
import com.composeship.feature.firebasedeploy.domain.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlaceholderFirebaseService : FirebaseService {
    override fun deploy(projectRoot: String): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Stdout("Local deployment is not supported on Web/Mobile. Use the Desktop version to deploy from local files."))
        emit(ProcessOutput.Complete(1))
    }

    override fun deployFromGitHub(repoUrl: String, branch: String): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Stdout("GitHub deployment on Web requires a backend service. This feature is currently in development for the Web platform."))
        emit(ProcessOutput.Complete(1))
    }
}
