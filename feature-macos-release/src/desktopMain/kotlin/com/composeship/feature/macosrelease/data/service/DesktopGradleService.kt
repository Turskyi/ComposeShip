package com.composeship.feature.macosrelease.data.service

import com.composeship.feature.macosrelease.domain.service.GradleService
import com.composeship.feature.macosrelease.domain.service.ProcessOutput
import com.composeship.feature.macosrelease.domain.service.ProcessService
import kotlinx.coroutines.flow.Flow
import java.io.File

class DesktopGradleService(
    private val processService: ProcessService
) : GradleService {

    override fun runTask(
        projectRoot: String,
        taskName: String,
        arguments: List<String>
    ): Flow<ProcessOutput> {
        val gradlew = if (System.getProperty("os.name").lowercase().contains("win")) {
            "gradlew.bat"
        } else {
            "./gradlew"
        }
        
        val command = mutableListOf(gradlew, taskName)
        command.addAll(arguments)
        
        return processService.execute(
            command = command,
            directory = projectRoot
        )
    }

    override fun findAvailableTasks(projectRoot: String): List<String> {
        // This could be implemented by running `./gradlew tasks`, 
        // but for now, we might just want to validate common ones 
        // or let the user know if a specific one is missing.
        return emptyList() 
    }
}
