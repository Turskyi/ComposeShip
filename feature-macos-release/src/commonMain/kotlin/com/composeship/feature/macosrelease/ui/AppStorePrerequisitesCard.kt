package com.composeship.feature.macosrelease.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.appstore_prerequisites_desc
import composeship.core.generated.resources.appstore_prerequisites_title
import composeship.core.generated.resources.open_app_store_connect
import composeship.core.generated.resources.open_developer_portal
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppStorePrerequisitesCard(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val bundleIdUrl = "https://developer.apple.com/account/resources/identifiers/bundleId/add/bundle"
    val appStoreConnectUrl = "https://appstoreconnect.apple.com/apps"

    OutlinedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.appstore_prerequisites_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.appstore_prerequisites_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                TextButton(
                    onClick = { uriHandler.openUri(bundleIdUrl) }
                ) {
                    Text(stringResource(Res.string.open_developer_portal))
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { uriHandler.openUri(appStoreConnectUrl) }
                ) {
                    Text(stringResource(Res.string.open_app_store_connect))
                }
            }
        }
    }
}
