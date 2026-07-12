package com.composeship.feature.macosrelease.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.ui.TechnicalGridBackground
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.api_key_link_text
import composeship.core.generated.resources.api_key_path_help
import composeship.core.generated.resources.api_key_path_label
import composeship.core.generated.resources.app_identity_label
import composeship.core.generated.resources.back
import composeship.core.generated.resources.browse
import composeship.core.generated.resources.error_invalid_issuer_id
import composeship.core.generated.resources.error_invalid_key_id
import composeship.core.generated.resources.hide_details
import composeship.core.generated.resources.identity_app_store_desc
import composeship.core.generated.resources.identity_developer_id_desc
import composeship.core.generated.resources.installer_identity_explanation
import composeship.core.generated.resources.installer_identity_label
import composeship.core.generated.resources.issuer_id_help
import composeship.core.generated.resources.issuer_id_label
import composeship.core.generated.resources.key_id_help
import composeship.core.generated.resources.key_id_label
import composeship.core.generated.resources.logs
import composeship.core.generated.resources.macos_release_title
import composeship.core.generated.resources.next
import composeship.core.generated.resources.no_app_identities
import composeship.core.generated.resources.no_installer_identities
import composeship.core.generated.resources.open_apple_developer
import composeship.core.generated.resources.project_root_label
import composeship.core.generated.resources.refresh
import composeship.core.generated.resources.released_successfully
import composeship.core.generated.resources.show_details
import composeship.core.generated.resources.start_over
import composeship.core.generated.resources.start_release
import composeship.core.generated.resources.step_1_title
import composeship.core.generated.resources.step_2_title
import composeship.core.generated.resources.step_3_title
import composeship.core.generated.resources.step_4_title
import composeship.core.generated.resources.step_5_title
import composeship.core.generated.resources.step_category_title
import composeship.core.generated.resources.task_debug_desc
import composeship.core.generated.resources.task_release_desc
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun MacOsReleaseScreen(
    viewModel: MacOsReleaseViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        TechnicalGridBackground()

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 2.dp,
            border =
                if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                    null
                else
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                    )
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.step != ReleaseStep.SelectProject && !state.isReleasing) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                    Text(
                        text = stringResource(Res.string.macos_release_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    }
                ) { step ->
                    when (step) {
                        ReleaseStep.SelectProject -> ProjectSelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.SelectTask -> TaskSelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.AppCategory -> CategorySelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.SigningIdentity -> IdentitySelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.AppStoreCredentials -> CredentialsStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.Process -> ReleaseProcessStep(
                            state,
                            viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectSelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    Column {
        Text(
            stringResource(Res.string.step_1_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.projectRoot,
                onValueChange = { viewModel.onProjectRootChanged(it) },
                label = { Text(stringResource(Res.string.project_root_label)) },
                modifier = Modifier.weight(1f),
                isError = state.projectValidationError != null,
                supportingText = {
                    state.projectValidationError?.let {
                        Text(it)
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.onBrowseProjectRoot() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(Res.string.browse))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.nextStep() },
            enabled = state.isProjectValid && state.projectRoot.isNotEmpty()
        ) {
            Text(stringResource(Res.string.next))
        }
    }
}

@Composable
fun TaskSelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    var showDebugWarning by remember { mutableStateOf(false) }

    Column {
        Text(
            stringResource(Res.string.step_2_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        state.availableTasks.forEach { task ->
            val isRelease = task.contains("Release", ignoreCase = true)
            val descRes =
                if (isRelease) Res.string.task_release_desc else Res.string.task_debug_desc

            Surface(
                onClick = {
                    viewModel.onTaskSelected(task)
                    if (!isRelease) showDebugWarning = true
                },
                shape = MaterialTheme.shapes.medium,
                color = if (state.selectedTask == task) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedTask == task,
                        onClick = {
                            viewModel.onTaskSelected(task)
                            if (!isRelease) showDebugWarning = true
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = task,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (!isRelease && state.selectedTask == task) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(descRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showDebugWarning && !state.selectedTask.contains(
                "Release",
                ignoreCase = true
            )
        ) {
            Text(
                "⚠️ Warning: packagePkg is for local testing. Do not use for App Store submission.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            OutlinedButton(onClick = { viewModel.previousStep() }) {
                Text(stringResource(Res.string.back))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.nextStep() }) {
                Text(stringResource(Res.string.next))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            stringResource(Res.string.step_category_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Application Category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                state.availableCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            viewModel.onCategorySelected(category)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            OutlinedButton(onClick = { viewModel.previousStep() }) {
                Text(stringResource(Res.string.back))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.nextStep() }) {
                Text(stringResource(Res.string.next))
            }
        }
    }
}

@Composable
fun IdentitySelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    val uriHandler = LocalUriHandler.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.step_3_title),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { viewModel.loadSigningIdentities() }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(Res.string.refresh))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoadingIdentities) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }

        Text(
            stringResource(Res.string.app_identity_label),
            style = MaterialTheme.typography.titleSmall
        )
        if (state.signingIdentities.isEmpty() && !state.isLoadingIdentities) {
            Text(
                stringResource(Res.string.no_app_identities),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        state.signingIdentities.forEach { identity ->
            val isAppStore =
                identity.contains("3rd Party Mac Developer Application")
            val isDeveloperId = identity.contains("Developer ID Application")
            val description = when {
                isAppStore -> stringResource(Res.string.identity_app_store_desc)
                isDeveloperId -> stringResource(Res.string.identity_developer_id_desc)
                else -> null
            }

            Surface(
                onClick = { viewModel.onIdentitySelected(identity) },
                shape = MaterialTheme.shapes.small,
                color = if (state.selectedIdentity == identity) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(
                        vertical = 4.dp,
                        horizontal = 8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedIdentity == identity,
                        onClick = { viewModel.onIdentitySelected(identity) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            identity,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (description != null) {
                            Text(
                                description,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDeveloperId) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(Res.string.installer_identity_label),
            style = MaterialTheme.typography.titleSmall
        )
        if (state.installerIdentities.isEmpty() && !state.isLoadingIdentities) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        MaterialTheme.shapes.small
                    )
                    .padding(12.dp)
            ) {
                Text(
                    stringResource(Res.string.no_installer_identities),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.installer_identity_explanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { uriHandler.openUri("https://developer.apple.com/account/resources/certificates/list") }
                ) {
                    Text(stringResource(Res.string.open_apple_developer))
                }
            }
        } else {
            state.installerIdentities.forEach { identity ->
                Surface(
                    onClick = { viewModel.onInstallerIdentitySelected(identity) },
                    shape = MaterialTheme.shapes.small,
                    color = if (state.selectedInstallerIdentity == identity) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(
                            vertical = 4.dp,
                            horizontal = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.selectedInstallerIdentity == identity,
                            onClick = {
                                viewModel.onInstallerIdentitySelected(
                                    identity
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            identity,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            OutlinedButton(onClick = { viewModel.previousStep() }) {
                Text(stringResource(Res.string.back))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.nextStep() },
                enabled = state.selectedIdentity.isNotEmpty() &&
                        state.selectedInstallerIdentity.isNotEmpty()
            ) {
                Text(stringResource(Res.string.next))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CredentialsStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    val uriHandler = LocalUriHandler.current
    val apiPageUrl = "https://appstoreconnect.apple.com/access/integrations/api"

    Column {
        Text(
            stringResource(Res.string.step_4_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        CredentialField(
            value = state.apiIssuerId,
            onValueChange = {
                viewModel.onCredentialsChanged(
                    it,
                    state.apiKeyId,
                    state.apiKeyPath
                )
            },
            label = stringResource(Res.string.issuer_id_label),
            helpText = stringResource(Res.string.issuer_id_help),
            isError = !state.isIssuerIdValid,
            errorText = stringResource(Res.string.error_invalid_issuer_id),
            onActionClick = { uriHandler.openUri(apiPageUrl) },
            actionLabel = stringResource(Res.string.api_key_link_text)
        )

        CredentialField(
            value = state.apiKeyId,
            onValueChange = {
                viewModel.onCredentialsChanged(
                    state.apiIssuerId,
                    it,
                    state.apiKeyPath
                )
            },
            label = stringResource(Res.string.key_id_label),
            helpText = stringResource(Res.string.key_id_help),
            isError = !state.isKeyIdValid,
            errorText = stringResource(Res.string.error_invalid_key_id),
            onActionClick = { uriHandler.openUri(apiPageUrl) },
            actionLabel = stringResource(Res.string.api_key_link_text)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CredentialField(
                    value = state.apiKeyPath,
                    onValueChange = {
                        viewModel.onCredentialsChanged(
                            state.apiIssuerId,
                            state.apiKeyId,
                            it
                        )
                    },
                    label = stringResource(Res.string.api_key_path_label),
                    helpText = stringResource(Res.string.api_key_path_help),
                    isError = false,
                    errorText = ""
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.onBrowseApiKey() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(Res.string.browse))
            }
        }

        if (state.detectedApiKeyFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (state.detectedApiKeyFiles.size == 1) "Auto-detected key:" else "Multiple keys found — select one to fill fields:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.detectedApiKeyFiles.forEach { path ->
                    val filename = path.substringAfterLast("/")
                    FilterChip(
                        selected = state.apiKeyPath == path,
                        onClick = {
                            val keyIdMatch =
                                Regex("AuthKey_([A-Z0-9]{10})\\.p8").find(
                                    filename
                                )
                            val detectedKeyId = keyIdMatch?.groupValues?.get(1)
                                ?: state.apiKeyId
                            viewModel.onCredentialsChanged(
                                state.apiIssuerId,
                                detectedKeyId,
                                path
                            )
                        },
                        label = {
                            Text(
                                filename,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            OutlinedButton(onClick = { viewModel.previousStep() }) {
                Text(stringResource(Res.string.back))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.nextStep() }) {
                Text(stringResource(Res.string.next))
            }
        }
    }
}

@Composable
fun CredentialField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    helpText: String,
    isError: Boolean,
    errorText: String,
    onActionClick: (() -> Unit)? = null,
    actionLabel: String? = null
) {
    var showHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.weight(1f),
                isError = isError,
                supportingText = {
                    if (isError) Text(errorText)
                }
            )
            IconButton(onClick = { showHelp = !showHelp }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Help",
                    tint = if (showHelp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showHelp) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(top = 4.dp, end = 48.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = helpText,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (onActionClick != null && actionLabel != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onActionClick,
                            contentPadding = PaddingValues(
                                vertical = 0.dp,
                                horizontal = 8.dp
                            )
                        ) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelMedium,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReleaseProcessStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    var showLogs by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var autoScroll by remember { mutableStateOf(true) }
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
                enabled = !state.isReleasing && !state.releaseSuccess
            ) {
                Text(
                    if (state.releaseSuccess)
                        stringResource(Res.string.released_successfully)
                    else
                        stringResource(Res.string.start_release),
                )
            }

            if (state.releaseSuccess || state.releaseError != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { viewModel.startOver() }) {
                    Text(stringResource(Res.string.start_over))
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
                    if (showLogs) stringResource(Res.string.hide_details) else stringResource(
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
                                    listState.animateScrollToItem(state.releaseLogs.size - 1)
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
                    val showJumpToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
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
                            Icon(Icons.Default.KeyboardArrowUp, "Jump to top")
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
