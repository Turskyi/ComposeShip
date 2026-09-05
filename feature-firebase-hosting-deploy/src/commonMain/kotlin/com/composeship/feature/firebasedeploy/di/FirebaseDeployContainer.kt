package com.composeship.feature.firebasedeploy.di

import com.composeship.core.domain.service.FileSystemService
import com.composeship.feature.firebasedeploy.domain.FirebaseService
import com.composeship.feature.firebasedeploy.ui.FirebaseDeployViewModel

class FirebaseDeployContainer(
    private val firebaseService: FirebaseService,
    private val fileSystemService: FileSystemService
) {
    fun createViewModel(): FirebaseDeployViewModel {
        return FirebaseDeployViewModel(
            firebaseService = firebaseService,
            fileSystemService = fileSystemService
        )
    }
}
