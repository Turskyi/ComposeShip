package com.composeship.feature.wearrelease.data.service

import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull

class DesktopCredentialService(
    private val processService: ProcessService
) {
    private val serviceName = "com.composeship.wearrelease"

    suspend fun saveCredential(key: String, value: String) {
        // delete first to avoid error
        deleteCredential(key)

        processService.execute(
            listOf(
                "security", "add-generic-password",
                "-a", key,
                "-s", serviceName,
                "-w", value,
                "-U"
            )
        ).filterIsInstance<ProcessOutput.Complete>().firstOrNull()
    }

    suspend fun getCredential(key: String): String? {
        val result = processService.execute(
            listOf(
                "security", "find-generic-password",
                "-a", key,
                "-s", serviceName,
                "-w"
            )
        ).filterIsInstance<ProcessOutput.Stdout>().firstOrNull()

        return result?.line
    }

    suspend fun deleteCredential(key: String) {
        processService.execute(
            listOf(
                "security", "delete-generic-password",
                "-a", key,
                "-s", serviceName
            )
        ).filterIsInstance<ProcessOutput.Complete>().firstOrNull()
    }
}

