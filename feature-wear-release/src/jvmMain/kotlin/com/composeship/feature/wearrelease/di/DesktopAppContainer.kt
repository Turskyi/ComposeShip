package com.composeship.feature.wearrelease.di

import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.wearrelease.data.service.DesktopCredentialService
import com.composeship.feature.wearrelease.ui.WearReleaseViewModel

class DesktopAppContainer(
    private val processService: ProcessService,
    private val fileSystemService: FileSystemService
) {
    private val credentialService: DesktopCredentialService = DesktopCredentialService(processService)

    fun createWearReleaseViewModel(): WearReleaseViewModel {
        return WearReleaseViewModel(processService = processService, fileSystemService = fileSystemService, credentialService = credentialService)
    }
}
