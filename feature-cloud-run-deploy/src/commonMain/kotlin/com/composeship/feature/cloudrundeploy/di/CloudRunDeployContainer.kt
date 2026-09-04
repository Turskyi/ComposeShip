package com.composeship.feature.cloudrundeploy.di

import com.composeship.core.domain.service.FileSystemService
import com.composeship.feature.cloudrundeploy.domain.GcloudService
import com.composeship.feature.cloudrundeploy.ui.CloudRunDeployViewModel

class CloudRunDeployContainer(
    private val gcloudService: GcloudService,
    private val fileSystemService: FileSystemService
) {
    fun createViewModel(): CloudRunDeployViewModel {
        return CloudRunDeployViewModel(
            gcloudService = gcloudService,
            fileSystemService = fileSystemService
        )
    }
}
