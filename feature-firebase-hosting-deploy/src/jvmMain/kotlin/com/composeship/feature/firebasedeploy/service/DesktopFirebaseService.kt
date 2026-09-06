package com.composeship.feature.firebasedeploy.service

import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.firebasedeploy.domain.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.nio.file.Files

class DesktopFirebaseService(
    private val processService: ProcessService
) : FirebaseService {
    override fun deploy(projectRoot: String): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Stdout("Building WASM distribution..."))
        
        val buildCommand = listOf("./gradlew", ":composeApp:wasmJsBrowserDistribution")
        
        var buildExitCode = -1
        processService.execute(buildCommand, directory = projectRoot).collect { output ->
            if (output is ProcessOutput.Complete) {
                buildExitCode = output.exitCode
            } else {
                emit(output)
            }
        }

        if (buildExitCode == 0) {
            emit(ProcessOutput.Stdout("Deploying to Firebase Hosting..."))
            val deployCommand = listOf("firebase", "deploy", "--only", "hosting")
            emitAll(processService.execute(deployCommand, directory = projectRoot))
        } else if (buildExitCode != -1) {
            emit(ProcessOutput.Complete(buildExitCode))
        }
    }

    override fun deployFromGitHub(repoUrl: String, branch: String): Flow<ProcessOutput> = flow {
        val tempDir = Files.createTempDirectory("composeship-firebase")
        val tempPath = tempDir.toAbsolutePath().toString()

        try {
            emit(ProcessOutput.Stdout("Cloning repository $repoUrl (branch: $branch)..."))
            val cloneCommand = listOf("git", "clone", "-b", branch, "--single-branch", repoUrl, ".")

            var cloneExitCode = -1
            processService.execute(cloneCommand, directory = tempPath).collect { output ->
                if (output is ProcessOutput.Complete) {
                    cloneExitCode = output.exitCode
                } else {
                    emit(output)
                }
            }

            if (cloneExitCode == 0) {
                emitAll(deploy(tempPath))
            } else if (cloneExitCode != -1) {
                emit(ProcessOutput.Complete(cloneExitCode))
            }
        } catch (e: Exception) {
            emit(ProcessOutput.Error(e))
        }
    }
}
