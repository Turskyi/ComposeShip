package com.composeship.ui.base64

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.composeship.app.shared.generated.resources.Res
import com.composeship.app.shared.generated.resources.base64_privacy_note
import com.composeship.app.shared.generated.resources.base64_tab_decode
import com.composeship.app.shared.generated.resources.base64_tab_encode
import com.composeship.app.shared.generated.resources.base64_title
import com.composeship.core.ui.TechnicalGridBackground
import org.jetbrains.compose.resources.stringResource

@Composable
fun Base64Screen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.base64_tab_decode),
        stringResource(Res.string.base64_tab_encode)
    )

    Box(modifier = modifier.fillMaxSize()) {
        TechnicalGridBackground()
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            border = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) null
            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(Res.string.base64_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(Res.string.base64_privacy_note), style = MaterialTheme.typography.bodyMedium)

                ScrollableTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> Base64DecodeSection()
                    1 -> Base64EncodeSection()
                }
            }
        }
    }
}
