package com.composeship.feature.wearrelease.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.wear_save_credentials_info_text
import composeship.core.generated.resources.wear_save_credentials_info_title
import composeship.core.generated.resources.wear_save_credentials_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionControlSection(
    state: WearReleaseState,
    viewModel: WearReleaseViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { viewModel.saveCredentials() },
                enabled = !state.buildInProgress
            ) {
                Text(stringResource(Res.string.wear_save_credentials_label))
            }
            InfoButton(
                title = stringResource(Res.string.wear_save_credentials_info_title),
                text = stringResource(Res.string.wear_save_credentials_info_text)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val uploadEnabled =
                !state.buildInProgress && state.serviceAccountPath.isNotBlank() && state.packageName.isNotBlank()
            Button(
                onClick = { viewModel.prepareAndUpload(state.projectRoot) },
                enabled = uploadEnabled
            ) {
                Text(if (state.track == "production") "Upload to Play (production)" else "Upload to Play")
            }
        }

        if (state.statusMessage != null) {
            Text(
                text = state.statusMessage,
                color = if (state.isStatusError) Color.Red else Color(0xFF008000),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (state.needsConfirmation) {
            Text(
                "Production upload requires confirmation — press Upload again to confirm.",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
