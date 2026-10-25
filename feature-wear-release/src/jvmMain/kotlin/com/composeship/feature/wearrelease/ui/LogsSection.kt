package com.composeship.feature.wearrelease.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun LogsSection(
    state: WearReleaseState,
    modifier: Modifier = Modifier
) {
    val listState: LazyListState = rememberLazyListState()
    var autoScroll by remember { mutableStateOf(true) }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        ) {
            Text("Logs", modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    val fullLogs = state.lastOutputLines.joinToString("\n")
                    clipboardManager.setText(AnnotatedString(fullLogs))
                },
                enabled = state.lastOutputLines.isNotEmpty()
            ) {
                Text("Copy Logs")
            }
        }
        SelectionContainer {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(state.lastOutputLines) { line ->
                    Text(line)
                }
            }
        }

        LaunchedEffect(state.lastOutputLines.size) {
            if (autoScroll && state.lastOutputLines.isNotEmpty()) {
                listState.scrollToItem(state.lastOutputLines.size - 1)
            }
        }
    }
}
