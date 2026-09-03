package com.composeship.feature.cloudrundeploy.di

import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.cloudrundeploy.domain.GcloudService
import com.composeship.feature.cloudrundeploy.service.DesktopGcloudService
import com.composeship.feature.cloudrundeploy.ui.CloudRunDeployViewModel

class CloudRunDeployContainer(
    private val processService: ProcessService,
    private val fileSystemService: FileSystemService
) {
    private val gcloudService: GcloudService = DesktopGcloudService(processService)

    fun createViewModel(): CloudRunDeployViewModel {
        return CloudRunDeployViewModel(
            gcloudService = gcloudService,
            fileSystemService = fileSystemService
        )
    }
}
