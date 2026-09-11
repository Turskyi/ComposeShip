package com.composeship.feature.cloudrundeploy.ui

import com.composeship.core.domain.model.DeploySource
import com.composeship.core.domain.model.LogEntry

data class CloudRunDeployState(
    val projectRoot: String = "",
    val isProjectValid: Boolean = false,
    val projectValidationError: String? = null,
    
    val gcloudProjectId: String = "",
    /**
     * The Google Cloud region where the service will be deployed.
     * Defaulted to "us-central1" to qualify for the Google Cloud "Always Free" tier,
     * which provides 2 million requests, 180k vCPU-seconds, and 360k GiB-seconds monthly at no cost.
     * Regions in Europe or other locations may incur small charges for networking egress
     * or may not qualify for the same compute free limits.
     */
    val region: String = "us-central1",
    val serviceName: String = "",
    val envVars: Map<String, String> = emptyMap(),

    val deployLogs: List<LogEntry> = emptyList(),
    val isDeploying: Boolean = false,
    val deployError: String? = null,
    val deploySuccess: Boolean = false,

    val isAutoFetchingProjectId: Boolean = false,
    val autoFetchFailed: Boolean = false,

    val availableServices: List<String> = emptyList(),
    val isFetchingServices: Boolean = false,
    val serviceFetchFailed: Boolean = false,

    val deploySource: DeploySource = DeploySource.LOCAL,
    val githubRepoUrl: String = "",
    val githubBranch: String = "master"
)
