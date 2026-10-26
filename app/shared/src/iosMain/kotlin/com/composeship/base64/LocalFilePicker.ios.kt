package com.composeship.base64

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class LocalDocumentPickerDelegate(
    private val onFilePicked: (Result<LocalFile>) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            onFilePicked(Result.failure(IllegalStateException("No file was selected")))
            onFinished()
            return
        }
        onFilePicked(readLocalFileResult {
            val path = url.path ?: error("Unable to access the selected file")
            val data = NSFileManager.defaultManager.contentsAtPath(path)
                ?: error("Unable to read the selected file")
            val length = data.length.toInt()
            val bytes = data.bytes?.readBytes(length) ?: ByteArray(0)
            localFileFromBytes(url.lastPathComponent ?: "file", bytes)
        })
        onFinished()
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onFinished()
    }
}

@Composable
actual fun rememberLocalFilePicker(onFilePicked: (Result<LocalFile>) -> Unit): () -> Unit {
    var delegate by remember { mutableStateOf<LocalDocumentPickerDelegate?>(null) }
    return remember(onFilePicked) {
        {
            val root = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (root == null) {
                onFilePicked(Result.failure(IllegalStateException("Unable to open the file picker")))
            } else {
                var presenter: UIViewController = root
                while (presenter.presentedViewController != null) {
                    presenter = presenter.presentedViewController!!
                }
                val pickerDelegate = LocalDocumentPickerDelegate(onFilePicked) { delegate = null }
                delegate = pickerDelegate
                val picker = UIDocumentPickerViewController(
                    documentTypes = listOf("public.item"),
                    inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
                )
                picker.delegate = pickerDelegate
                presenter.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
