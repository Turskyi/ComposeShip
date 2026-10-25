package com.composeship.feature.cloudrundeploy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.domain.model.DeploySource
import com.composeship.core.domain.model.LogEntry
import com.composeship.core.domain.model.LogType
import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessOutput
import com.composeship.feature.cloudrundeploy.domain.GcloudService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CloudRunDeployViewModel(
    private val gcloudService: GcloudService,
    private val fileSystemService: FileSystemService
) : ViewModel() {

    private val _state = MutableStateFlow(CloudRunDeployState())
    val state: StateFlow<CloudRunDeployState> = _state.asStateFlow()

    private var deployJob: Job? = null

    fun onProjectRootChanged(path: String) {
        _state.update { 
            it.copy(
                projectRoot = path, 
                projectValidationError = null, 
                autoFetchFailed = false
            ) 
        }
        validateProject(path)
    }

    fun onBrowseProjectRoot() {
        viewModelScope.launch {
            val path = fileSystemService.pickDirectory()
            if (path != null) {
                onProjectRootChanged(path)
            }
        }
    }

    fun onGcloudProjectIdChanged(id: String) {
        _state.update { it.copy(gcloudProjectId = id) }
    }

    fun onRegionChanged(region: String) {
        _state.update { it.copy(region = region) }
        fetchServices()
    }

    fun fetchServices() {
        val currentState = _state.value
        if (currentState.gcloudProjectId.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isFetchingServices = true, serviceFetchFailed = false) }
            val services = mutableListOf<String>()
            gcloudService.listServices(currentState.gcloudProjectId, currentState.region).collect { output ->
                when (output) {
                    is ProcessOutput.Stdout -> {
                        val name = output.line.trim()
                        if (name.isNotEmpty()) services.add(name)
                    }
                    is ProcessOutput.Complete -> {
                        _state.update { 
                            it.copy(
                                isFetchingServices = false, 
                                availableServices = services,
                                serviceName = when {
                                    services.size == 1 -> services.first()
                                    services.isEmpty() && it.serviceName.isEmpty() -> suggestServiceName(it.projectRoot)
                                    else -> it.serviceName
                                }
                            ) 
                        }
                    }
                    is ProcessOutput.Error -> {
                        _state.update { 
                            it.copy(
                                isFetchingServices = false, 
                                serviceFetchFailed = true,
                                serviceName = if (it.serviceName.isEmpty()) suggestServiceName(it.projectRoot) else it.serviceName
                            ) 
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun suggestServiceName(path: String): String {
        if (path.isEmpty()) return ""
        return path.substringAfterLast("/").lowercase().replace(Regex("[^a-z0-9-]"), "-")
    }

    fun onServiceNameChanged(name: String) {
        _state.update { it.copy(serviceName = name) }
    }

    fun onDeploySourceChanged(source: DeploySource) {
        _state.update { it.copy(deploySource = source) }
    }

    fun onGithubRepoUrlChanged(url: String) {
        val branchFromUrl = parseGithubRepoUrl(url)?.branch
        _state.update {
            it.copy(
                githubRepoUrl = url,
                githubBranch = branchFromUrl ?: it.githubBranch,
                githubValidationError = null,
                deployError = null
            )
        }
    }

    fun onGithubBranchChanged(branch: String) {
        _state.update { it.copy(githubBranch = branch, githubValidationError = null, deployError = null) }
    }

    fun validateGithubConfig(repoUrl: String = _state.value.githubRepoUrl, branch: String = _state.value.githubBranch): String? {
        if (repoUrl.isBlank()) return "GitHub repository URL is required"

        val parsedRepo = parseGithubRepoUrl(repoUrl)
        if (parsedRepo == null) {
            return "Use a valid GitHub HTTPS URL, for example: https://github.com/user/repo"
        }

        val selectedBranch = branch.trim()
        if (selectedBranch.isBlank()) return "Git branch is required"
        if (selectedBranch.contains(" ")) return "Branch name cannot contain spaces"
        if (selectedBranch == "." || selectedBranch == "..") return "Invalid branch name"

        return null
    }

    private data class ParsedGithubRepo(val repositoryUrl: String, val branch: String?)

    private fun parseGithubRepoUrl(url: String): ParsedGithubRepo? {
        val cleanedUrl = url.trim().substringBefore('?').substringBefore('#').trimEnd('/')
        val treeUrlMatch = githubTreeUrlPattern.matchEntire(cleanedUrl)
        val repositoryUrl = treeUrlMatch?.groupValues?.get(1) ?: cleanedUrl
        if (!githubRepoUrlPattern.matches(repositoryUrl)) return null

        return ParsedGithubRepo(repositoryUrl, treeUrlMatch?.groupValues?.get(2))
    }

    private companion object {
        val githubRepoUrlPattern =
            Regex("^https?://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+(?:\\.git)?$")
        val githubTreeUrlPattern =
            Regex("^(https?://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+(?:\\.git)?)/tree/(.+)$")
    }

    fun autoFetchProjectId() {
        viewModelScope.launch {
            _state.update { it.copy(isAutoFetchingProjectId = true, autoFetchFailed = false) }
            var detectedId: String? = null

            gcloudService.getCurrentProjectId().collect { output ->
                when (output) {
                    is ProcessOutput.Stdout -> detectedId = output.line.trim()
                    is ProcessOutput.Complete -> {
                        _state.update { it.copy(isAutoFetchingProjectId = false) }
                        if (output.exitCode == 0 && !detectedId.isNullOrBlank()) {
                            onGcloudProjectIdChanged(detectedId)
                            fetchServices()
                        } else {
                            _state.update { it.copy(autoFetchFailed = true) }
                        }
                    }
                    is ProcessOutput.Error -> {
                        _state.update { it.copy(isAutoFetchingProjectId = false, autoFetchFailed = true) }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun validateProject(path: String) {
        if (path.isEmpty()) return

        val dockerfileMatch = fileSystemService.exists("$path/Dockerfile")
        val serverDirMatch = fileSystemService.exists("$path/server")

        if (!dockerfileMatch || !serverDirMatch) {
            _state.update {
                it.copy(
                    isProjectValid = false,
                    projectValidationError = "Project must contain a Dockerfile and a 'server' directory"
                )
            }
            return
        }

        loadEnvVars(path)

        _state.update {
            it.copy(
                isProjectValid = true,
                projectValidationError = null
            )
        }
    }

    private fun loadEnvVars(path: String) {
        val envPath = "$path/server/.env"
        if (fileSystemService.exists(envPath)) {
            val content = fileSystemService.readFile(envPath) ?: return
            val vars = mutableMapOf<String, String>()
            content.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    // Remove trailing comments
                    val cleanLine = trimmed.substringBefore(" #").trim()
                    val parts = cleanLine.split("=", limit = 2)
                    if (parts.size == 2) {
                        vars[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
            _state.update { it.copy(envVars = vars) }
        } else {
            _state.update { it.copy(envVars = emptyMap()) }
        }
    }

    fun startDeploy() {
        val currentState = _state.value
        if (currentState.gcloudProjectId.isEmpty()) {
            _state.update { it.copy(deployError = "Google Cloud Project ID is required") }
            return
        }

        if (currentState.deploySource == DeploySource.GITHUB) {
            val githubError = validateGithubConfig(currentState.githubRepoUrl, currentState.githubBranch)
            if (githubError != null) {
                _state.update { it.copy(githubValidationError = githubError, deployError = githubError) }
                return
            }
        }

        deployJob?.cancel()
        deployJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isDeploying = true,
                    deployLogs = emptyList(),
                    deployError = null,
                    deploySuccess = false
                )
            }

            val deployFlow = if (currentState.deploySource == DeploySource.LOCAL) {
                gcloudService.deploy(
                    projectRoot = currentState.projectRoot,
                    gcloudProjectId = currentState.gcloudProjectId,
                    region = currentState.region,
                    serviceName = currentState.serviceName,
                    envVars = currentState.envVars
                )
            } else {
                val githubRepo = parseGithubRepoUrl(currentState.githubRepoUrl)
                gcloudService.deployFromGitHub(
                    githubRepoUrl = githubRepo?.repositoryUrl ?: currentState.githubRepoUrl,
                    githubBranch = currentState.githubBranch,
                    gcloudProjectId = currentState.gcloudProjectId,
                    region = currentState.region,
                    serviceName = currentState.serviceName,
                    envVars = currentState.envVars
                )
            }

            deployFlow.collect { output ->
                when (output) {
                    is ProcessOutput.Stdout -> appendLog(output.line)
                    is ProcessOutput.Stderr -> appendLog(output.line, LogType.Error)
                    is ProcessOutput.Complete -> {
                        _state.update { it.copy(isDeploying = false) }
                        if (output.exitCode == 0) {
                            appendLog("Deployment successful!", LogType.Success)
                            _state.update { it.copy(deploySuccess = true) }
                        } else {
                            _state.update { it.copy(deployError = "Deployment failed with exit code ${output.exitCode}") }
                        }
                        deployJob = null
                    }
                    is ProcessOutput.Error -> {
                        _state.update {
                            it.copy(
                                isDeploying = false,
                                deployError = output.throwable.message ?: "Unknown error"
                            )
                        }
                        deployJob = null
                    }
                }
            }
        }
    }

    fun stopDeploy() {
        deployJob?.cancel()
        deployJob = null
        _state.update { 
            it.copy(
                isDeploying = false,
                deployError = "Deployment cancelled by user"
            ) 
        }
        appendLog("Deployment stopped by user.", LogType.Error)
    }

    private fun appendLog(message: String, type: LogType = LogType.Info) {
        _state.update {
            it.copy(
                deployLogs = it.deployLogs + LogEntry(message, type)
            )
        }
    }
}
