package com.composeship.feature.firebasedeploy.domain

import com.composeship.core.domain.service.ProcessOutput
import kotlinx.coroutines.flow.Flow

interface FirebaseService {
    fun deploy(projectRoot: String): Flow<ProcessOutput>
    fun deployFromGitHub(repoUrl: String, branch: String): Flow<ProcessOutput>
}
