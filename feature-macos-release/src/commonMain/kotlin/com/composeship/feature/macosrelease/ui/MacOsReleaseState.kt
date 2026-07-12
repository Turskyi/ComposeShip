package com.composeship.feature.macosrelease.ui

data class MacOsReleaseState(
    val step: ReleaseStep = ReleaseStep.SelectProject,
    val projectRoot: String = "",
    val isProjectValid: Boolean = false,
    val projectValidationError: String? = null,
    
    val availableTasks: List<String> = emptyList(),
    val selectedTask: String = "",
    
    val availableCategories: List<String> = emptyList(),
    val selectedCategory: String = "public.app-category.developer-tools",
    
    val buildOutput: List<LogEntry> = emptyList(),
    val isBuilding: Boolean = false,
    val buildError: String? = null,
    
    val signingIdentities: List<String> = emptyList(),
    val selectedIdentity: String = "",
    val installerIdentities: List<String> = emptyList(),
    val selectedInstallerIdentity: String = "",
    val isLoadingIdentities: Boolean = false,
    
    val apiIssuerId: String = "",
    val isIssuerIdValid: Boolean = true,
    val apiKeyId: String = "",
    val isKeyIdValid: Boolean = true,
    val apiKeyPath: String = "",
    val detectedApiKeyFiles: List<String> = emptyList(),
    
    val releaseLogs: List<LogEntry> = emptyList(),
    val isReleasing: Boolean = false,
    val releaseError: String? = null,
    val releaseSuccess: Boolean = false,
    val appStoreId: String? = null
)

enum class ReleaseStep {
    SelectProject,
    SelectTask,
    AppCategory,
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
