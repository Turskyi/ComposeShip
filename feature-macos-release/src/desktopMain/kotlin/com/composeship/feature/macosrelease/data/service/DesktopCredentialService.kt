package com.composeship.feature.macosrelease.data.service

import com.composeship.feature.macosrelease.domain.service.CredentialService
import com.composeship.feature.macosrelease.domain.service.ProcessOutput
import com.composeship.feature.macosrelease.domain.service.ProcessService
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DesktopCredentialService(
    private val processService: ProcessService
) : CredentialService {

    private val serviceName = "com.composeship.macosrelease"

    override suspend fun saveCredential(key: String, value: String) {
        // Delete first to avoid error if exists
        deleteCredential(key)
        
        processService.execute(
            listOf(
                "security", "add-generic-password",
                "-a", key,
                "-s", serviceName,
                "-w", value,
                "-U" // Update if exists (though we delete first)
            )
        ).filterIsInstance<ProcessOutput.Complete>().firstOrNull()
    }

    override suspend fun getCredential(key: String): String? {
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

    override suspend fun deleteCredential(key: String) {
        processService.execute(
            listOf(
                "security", "delete-generic-password",
                "-a", key,
                "-s", serviceName
            )
        ).filterIsInstance<ProcessOutput.Complete>().firstOrNull()
    }
}
