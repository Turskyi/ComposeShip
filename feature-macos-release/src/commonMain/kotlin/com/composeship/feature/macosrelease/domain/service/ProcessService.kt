package com.composeship.feature.macosrelease.domain.service

import kotlinx.coroutines.flow.Flow

interface ProcessService {
    fun execute(
        command: List<String>,
        directory: String? = null,
        env: Map<String, String> = emptyMap()
    ): Flow<ProcessOutput>
}

sealed class ProcessOutput {
    data class Stdout(val line: String) : ProcessOutput()
    data class Stderr(val line: String) : ProcessOutput()
    data class Complete(val exitCode: Int) : ProcessOutput()
    data class Error(val throwable: Throwable) : ProcessOutput()
}
