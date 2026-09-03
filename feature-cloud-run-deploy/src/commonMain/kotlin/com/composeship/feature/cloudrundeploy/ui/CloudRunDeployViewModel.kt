package com.composeship.feature.cloudrundeploy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.domain.model.LogEntry
import com.composeship.core.domain.model.LogType
import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessOutput
import com.composeship.feature.cloudrundeploy.domain.GcloudService
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
                            onGcloudProjectIdChanged(detectedId!!)
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

        _state.update {
            it.copy(
                isProjectValid = true,
                projectValidationError = null
            )
        }
    }

    fun startDeploy() {
        val currentState = _state.value
        if (currentState.gcloudProjectId.isEmpty()) {
            _state.update { it.copy(deployError = "Google Cloud Project ID is required") }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isDeploying = true,
                    deployLogs = emptyList(),
                    deployError = null,
                    deploySuccess = false
                )
            }

            gcloudService.deploy(
                projectRoot = currentState.projectRoot,
                gcloudProjectId = currentState.gcloudProjectId,
                region = currentState.region,
                serviceName = currentState.serviceName
            ).collect { output ->
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
                    }
                    is ProcessOutput.Error -> {
                        _state.update {
                            it.copy(
                                isDeploying = false,
                                deployError = output.throwable.message ?: "Unknown error"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun appendLog(message: String, type: LogType = LogType.Info) {
        _state.update {
            it.copy(
                deployLogs = it.deployLogs + LogEntry(message, type)
            )
        }
    }
}
