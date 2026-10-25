package com.composeship.feature.macosrelease.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.composeship.core.domain.model.LogType
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.back
import composeship.core.generated.resources.hide_details
import composeship.core.generated.resources.logs
import composeship.core.generated.resources.released_successfully
import composeship.core.generated.resources.show_details
import composeship.core.generated.resources.start_over
import composeship.core.generated.resources.start_release
import composeship.core.generated.resources.step_5_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReleaseProcessStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    var showLogs by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var autoScroll by remember { mutableStateOf(true) }

    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    // Fixed auto-scroll logic: use scrollToItem for immediate response
    LaunchedEffect(state.releaseLogs.size) {
        if (autoScroll && state.releaseLogs.isNotEmpty()) {
            listState.scrollToItem(state.releaseLogs.size - 1)
        }
    }

    // Detect if user scrolls away from bottom
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            if (lastVisibleItem != null) {
                val isAtBottom =
                    lastVisibleItem.index == state.releaseLogs.size - 1
                if (!isAtBottom) {
                    autoScroll = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            stringResource(Res.string.step_5_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.isReleasing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            if (!state.isReleasing && !state.releaseSuccess) {
                OutlinedButton(onClick = { viewModel.previousStep() }) {
                    Text(stringResource(Res.string.back))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = { viewModel.startRelease() },
                enabled = !state.isReleasing && !state.releaseSuccess,
                modifier = if (state.isReleasing)
                    Modifier.weight(1f)
                else Modifier
            ) {
                Text(
                    if (state.releaseSuccess)
                        stringResource(Res.string.released_successfully)
                    else
                        stringResource(Res.string.start_release),
                )
            }

            if (state.isReleasing) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.stopRelease() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Stop")
                }
            }

            if ((state.releaseSuccess || state.releaseError != null) && !state.isReleasing) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { viewModel.startOver() }) {
                    Text(stringResource(Res.string.start_over))
                }
            }
        }

        if (state.releaseSuccess && state.appStoreId != null) {
            val uriHandler = LocalUriHandler.current
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        uriHandler.openUri(
                            "https://appstoreconnect.apple.com/apps/${state.appStoreId}/testflight/macos"
                        )
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open TestFlight")
                }
                OutlinedButton(
                    onClick = {
                        uriHandler.openUri(
                            "https://appstoreconnect.apple.com/apps/${state.appStoreId}/distribution/macos/version/inflight"
                        )
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Distribution")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.logs),
                    style = MaterialTheme.typography.titleSmall
                )
                if (showLogs && state.releaseLogs.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val text =
                                state.releaseLogs.joinToString(
                                    separator = "\n",
                                ) { it.message }
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy logs",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            TextButton(onClick = { showLogs = !showLogs }) {
                Text(
                    if (showLogs) stringResource(Res.string.hide_details)
                    else stringResource(
                        Res.string.show_details
                    )
                )
            }
        }

        if (showLogs) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        ),
                        MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                SelectionContainer {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.releaseLogs) { entry ->
                            Text(
                                text = entry.message,
                                color = when (entry.type) {
                                    LogType.Info -> MaterialTheme.colorScheme.onSurface
                                    LogType.Error -> MaterialTheme.colorScheme.error
                                    LogType.Success -> Color(0xFF4CAF50)
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Scroll controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!autoScroll && state.releaseLogs.size > 10) {
                        FloatingActionButton(
                            onClick = {
                                autoScroll = true
                                scope.launch {
                                    listState.animateScrollToItem(
                                        state.releaseLogs.size - 1,
                                    )
                                }
                            },
                            modifier = Modifier.size(40.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                "Jump to bottom"
                            )
                        }
                    }
                    val showJumpToTop by remember {
                        derivedStateOf {
                            listState.firstVisibleItemIndex > 0
                        }
                    }
                    if (showJumpToTop) {
                        FloatingActionButton(
                            onClick = {
                                autoScroll = false
                                scope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            },
                            modifier = Modifier.size(40.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                "Jump to top",
                            )
                        }
                    }
                }
            }
        }

        state.releaseError?.let {
            SelectionContainer {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
