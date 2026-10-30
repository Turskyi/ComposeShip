package com.composeship.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.composeship.MainViewModel
import com.composeship.app.shared.generated.resources.Res as SharedRes
import com.composeship.app.shared.generated.resources.home_title
import com.composeship.app.shared.generated.resources.more_options_description
import com.composeship.app.shared.generated.resources.privacy_title
import com.composeship.app.shared.generated.resources.support_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun MoreOptionsMenu(
    viewModel: MainViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(SharedRes.string.more_options_description)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(SharedRes.string.home_title)) },
                onClick = {
                    viewModel.showLandingPage()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Laptop,
                        contentDescription = null
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(SharedRes.string.support_title)) },
                onClick = {
                    viewModel.showSupportPage()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(SharedRes.string.privacy_title)) },
                onClick = {
                    viewModel.showPrivacyPage()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null
                    )
                }
            )
        }
    }
}
