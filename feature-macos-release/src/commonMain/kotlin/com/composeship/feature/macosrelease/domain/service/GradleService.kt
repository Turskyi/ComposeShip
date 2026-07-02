package com.composeship.feature.macosrelease.domain.service

import kotlinx.coroutines.flow.Flow

interface GradleService {
    fun runTask(
        projectRoot: String,
        taskName: String,
        arguments: List<String> = emptyList()
    ): Flow<ProcessOutput>
    
    fun findAvailableTasks(projectRoot: String): List<String>
}
