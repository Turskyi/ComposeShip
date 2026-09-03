package com.composeship.feature.cloudrundeploy.ui

import com.composeship.core.domain.model.LogEntry

data class CloudRunDeployState(
    val projectRoot: String = "",
    val isProjectValid: Boolean = false,
    val projectValidationError: String? = null,
    
    val gcloudProjectId: String = "",
    val region: String = "northamerica-northeast1",
    val serviceName: String = "",
    
    val deployLogs: List<LogEntry> = emptyList(),
    val isDeploying: Boolean = false,
    val deployError: String? = null,
    val deploySuccess: Boolean = false,

    val isAutoFetchingProjectId: Boolean = false,
    val autoFetchFailed: Boolean = false,

    val availableServices: List<String> = emptyList(),
    val isFetchingServices: Boolean = false,
    val serviceFetchFailed: Boolean = false
)
