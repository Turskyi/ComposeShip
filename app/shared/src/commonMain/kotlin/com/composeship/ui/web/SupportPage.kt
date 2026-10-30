package com.composeship.ui.web

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeship.core.ui.LinkifyText
import com.composeship.core.ui.TechnicalGridBackground
import com.composeship.app.shared.generated.resources.Res as SharedRes
import com.composeship.app.shared.generated.resources.support_description
import com.composeship.app.shared.generated.resources.support_email_title
import com.composeship.app.shared.generated.resources.support_telegram_title
import com.composeship.app.shared.generated.resources.support_title
import com.composeship.app.shared.generated.resources.support_web_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SupportPage(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedText by remember { mutableStateOf<String?>(null) }

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top header row with home button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onHomeClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "Home",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(SharedRes.string.support_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(SharedRes.string.support_description),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Card(modifier = Modifier.fillMaxWidth(0.9f)) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(SharedRes.string.support_email_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "dmytro@turskyi.com",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString("dmytro@turskyi.com"))
                                    copiedText = "dmytro@turskyi.com"
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = stringResource(SharedRes.string.support_web_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            LinkifyText(
                                text = "https://turskyi.com/#/support",
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString("https://turskyi.com/#/support"))
                                    copiedText = "https://turskyi.com/#/support"
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = stringResource(SharedRes.string.support_telegram_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            LinkifyText(
                                text = "https://t.me/+7PclTqQfEphjYTAy",
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString("https://t.me/+7PclTqQfEphjYTAy"))
                                    copiedText = "https://t.me/+7PclTqQfEphjYTAy"
                                }
                            )
                        }
                    }

                    AnimatedVisibility(visible = copiedText != null) {
                        Text(
                            text = "Copied to clipboard: $copiedText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
