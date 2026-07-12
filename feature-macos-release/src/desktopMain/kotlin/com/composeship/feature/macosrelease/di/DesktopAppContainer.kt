package com.composeship.feature.macosrelease.di

import com.composeship.feature.macosrelease.data.service.DesktopAppStoreConnectService
import com.composeship.feature.macosrelease.data.service.DesktopCredentialService
import com.composeship.feature.macosrelease.data.service.DesktopFileSystemService
import com.composeship.feature.macosrelease.data.service.DesktopGradleService
import com.composeship.feature.macosrelease.data.service.DesktopProcessService
import com.composeship.feature.macosrelease.domain.service.AppStoreConnectService
import com.composeship.feature.macosrelease.domain.service.CredentialService
import com.composeship.feature.macosrelease.domain.service.FileSystemService
import com.composeship.feature.macosrelease.domain.service.GradleService
import com.composeship.feature.macosrelease.domain.service.ProcessService
import com.composeship.feature.macosrelease.ui.MacOsReleaseViewModel

class DesktopAppContainer {
    private val processService: ProcessService = DesktopProcessService()
    private val fileSystemService: FileSystemService = DesktopFileSystemService()
    private val gradleService: GradleService = DesktopGradleService(processService)
    private val credentialService: CredentialService = DesktopCredentialService(processService)
    private val appStoreConnectService: AppStoreConnectService = DesktopAppStoreConnectService()

    fun createMacOsReleaseViewModel(): MacOsReleaseViewModel {
        return MacOsReleaseViewModel(
            processService = processService,
            gradleService = gradleService,
            fileSystemService = fileSystemService,
            credentialService = credentialService,
            appStoreConnectService = appStoreConnectService
        )
    }
}
