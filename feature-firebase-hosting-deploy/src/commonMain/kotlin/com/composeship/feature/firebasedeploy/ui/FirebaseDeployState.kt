package com.composeship.feature.firebasedeploy.ui

import com.composeship.core.domain.model.DeploySource
import com.composeship.core.domain.model.LogEntry

data class FirebaseDeployState(
    val projectRoot: String = "",
    val isProjectValid: Boolean = false,
    val projectValidationError: String? = null,
    
    val deployLogs: List<LogEntry> = emptyList(),
    val isDeploying: Boolean = false,
    val deployError: String? = null,
    val deploySuccess: Boolean = false,

    val deploySource: DeploySource = DeploySource.LOCAL,
    val githubRepoUrl: String = "",
    val githubBranch: String = "master"
)
