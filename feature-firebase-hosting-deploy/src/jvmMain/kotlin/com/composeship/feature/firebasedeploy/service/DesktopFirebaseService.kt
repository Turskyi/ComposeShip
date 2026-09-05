package com.composeship.feature.firebasedeploy.service

import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.firebasedeploy.domain.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class DesktopFirebaseService(
    private val processService: ProcessService
) : FirebaseService {
    override fun deploy(projectRoot: String): Flow<ProcessOutput> = flow {
        emit(ProcessOutput.Stdout("Building WASM distribution..."))
        
        val buildCommand = listOf("./gradlew", ":composeApp:wasmJsBrowserDistribution")
        
        var buildSuccess = false
        processService.execute(buildCommand, directory = projectRoot).collect { output ->
            emit(output)
            if (output is ProcessOutput.Complete && output.exitCode == 0) {
                buildSuccess = true
            }
        }

        if (buildSuccess) {
            emit(ProcessOutput.Stdout("Deploying to Firebase Hosting..."))
            val deployCommand = listOf("firebase", "deploy", "--only", "hosting")
            emitAll(processService.execute(deployCommand, directory = projectRoot))
        }
    }
}
