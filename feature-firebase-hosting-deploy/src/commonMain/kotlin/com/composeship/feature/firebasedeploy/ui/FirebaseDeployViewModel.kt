package com.composeship.feature.firebasedeploy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.domain.model.DeploySource
import com.composeship.core.domain.model.LogEntry
import com.composeship.core.domain.model.LogType
import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessOutput
import com.composeship.feature.firebasedeploy.domain.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FirebaseDeployViewModel(
    private val firebaseService: FirebaseService,
    private val fileSystemService: FileSystemService
) : ViewModel() {

    private val _state = MutableStateFlow(FirebaseDeployState())
    val state: StateFlow<FirebaseDeployState> = _state.asStateFlow()

    fun onProjectRootChanged(path: String) {
        _state.update { 
            it.copy(
                projectRoot = path, 
                projectValidationError = null
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

    private fun validateProject(path: String) {
        if (path.isEmpty()) return

        val firebaseJsonMatch = fileSystemService.exists("$path/firebase.json")
        val gradleBuildMatch = fileSystemService.exists("$path/composeApp/build.gradle.kts")

        if (!firebaseJsonMatch || !gradleBuildMatch) {
            _state.update {
                it.copy(
                    isProjectValid = false,
                    projectValidationError = "Project must contain firebase.json and a ':composeApp' module"
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

    fun onDeploySourceChanged(source: DeploySource) {
        _state.update { it.copy(deploySource = source) }
    }

    fun onGithubRepoUrlChanged(url: String) {
        _state.update { it.copy(githubRepoUrl = url) }
    }

    fun onGithubBranchChanged(branch: String) {
        _state.update { it.copy(githubBranch = branch) }
    }

    fun startDeploy() {
        val currentState = _state.value
        if (currentState.deploySource == DeploySource.LOCAL && (currentState.projectRoot.isEmpty() || !currentState.isProjectValid)) {
            _state.update { it.copy(deployError = "Valid Project Root is required") }
            return
        }

        if (currentState.deploySource == DeploySource.GITHUB && currentState.githubRepoUrl.isEmpty()) {
            _state.update { it.copy(deployError = "GitHub Repository URL is required") }
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

            val deployFlow = if (currentState.deploySource == DeploySource.LOCAL) {
                firebaseService.deploy(currentState.projectRoot)
            } else {
                firebaseService.deployFromGitHub(
                    repoUrl = currentState.githubRepoUrl,
                    branch = currentState.githubBranch
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
