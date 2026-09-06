package com.composeship.feature.wearrelease.ui

data class WearReleaseState(
    val projectRoot: String = "",
    val isProjectValid: Boolean = false,
    val buildInProgress: Boolean = false,
    val releaseNotes: String = "",
    val lastOutputLines: List<String> = emptyList(),

    // credentials / inputs
    val serviceAccountPath: String = "",
    val packageName: String = "",
    val keystorePath: String = "",
    val keystoreAlias: String = "",
    val track: String = "internal",
    val needsConfirmation: Boolean = false
)
