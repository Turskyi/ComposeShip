package com.composeship.ui.web

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.composeship.app.shared.generated.resources.Res as SharedRes
import com.composeship.app.shared.generated.resources.privacy_contact_desc
import com.composeship.app.shared.generated.resources.privacy_contact_title
import com.composeship.app.shared.generated.resources.privacy_data_collection_desc
import com.composeship.app.shared.generated.resources.privacy_data_collection_title
import com.composeship.app.shared.generated.resources.privacy_local_storage_desc
import com.composeship.app.shared.generated.resources.privacy_local_storage_title
import com.composeship.app.shared.generated.resources.privacy_overview_desc
import com.composeship.app.shared.generated.resources.privacy_overview_title
import com.composeship.app.shared.generated.resources.privacy_third_party_desc
import com.composeship.app.shared.generated.resources.privacy_third_party_title
import com.composeship.app.shared.generated.resources.privacy_title
import com.composeship.core.ui.TechnicalGridBackground
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacyPage(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {}
) {
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
                        text = stringResource(SharedRes.string.privacy_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SelectionContainer {
                        Card(modifier = Modifier.fillMaxWidth(0.9f)) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(SharedRes.string.privacy_overview_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(SharedRes.string.privacy_overview_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = stringResource(SharedRes.string.privacy_data_collection_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(SharedRes.string.privacy_data_collection_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = stringResource(SharedRes.string.privacy_local_storage_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(SharedRes.string.privacy_local_storage_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = stringResource(SharedRes.string.privacy_third_party_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(SharedRes.string.privacy_third_party_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = stringResource(SharedRes.string.privacy_contact_title),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(SharedRes.string.privacy_contact_desc),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
