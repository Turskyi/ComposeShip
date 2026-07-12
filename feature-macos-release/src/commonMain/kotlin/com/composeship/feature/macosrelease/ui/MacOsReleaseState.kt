package com.composeship.feature.macosrelease.ui

data class MacOsReleaseState(
    val step: ReleaseStep = ReleaseStep.SelectProject,
    val projectRoot: String = "",
    val isProjectValid: Boolean = false,
    val projectValidationError: String? = null,
    
    val availableTasks: List<String> = emptyList(),
    val selectedTask: String = "",
    
    val buildOutput: List<LogEntry> = emptyList(),
    val isBuilding: Boolean = false,
    val buildError: String? = null,
    
    val signingIdentities: List<String> = emptyList(),
    val selectedIdentity: String = "",
    val installerIdentities: List<String> = emptyList(),
    val selectedInstallerIdentity: String = "",
    val isLoadingIdentities: Boolean = false,
    
    val apiIssuerId: String = "",
    val apiKeyId: String = "",
    val apiKeyPath: String = "",
    
    val releaseLogs: List<LogEntry> = emptyList(),
    val isReleasing: Boolean = false,
    val releaseError: String? = null,
    val releaseSuccess: Boolean = false
)

enum class ReleaseStep {
    SelectProject,
    SelectTask,
    SigningIdentity,
    AppStoreCredentials,
    Process
}

data class LogEntry(
    val message: String,
    val type: LogType = LogType.Info
)

enum class LogType {
    Info, Error, Success
}
