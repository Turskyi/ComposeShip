package com.composeship.base64

import androidx.compose.runtime.Composable

@Composable
expect fun rememberLocalFilePicker(
    onFilePicked: (Result<LocalFile>) -> Unit,
): () -> Unit

internal fun <T> readLocalFileResult(read: () -> T): Result<T> =
    try {
        Result.success(read())
    } catch (exception: Exception) {
        Result.failure(exception)
    }
