package com.composeship.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.MainViewModel
import com.composeship.core.domain.model.Feature
import com.composeship.core.theme.ComposeShipTheme
import com.composeship.core.theme.ThemeMode
import com.composeship.getPlatform
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    viewModel: MainViewModel,
    content: @Composable (Modifier) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val selectedLanguageCode by viewModel.selectedLanguageCode.collectAsStateWithLifecycle()
    val currentFeature by viewModel.currentFeature.collectAsStateWithLifecycle()
    val platform = remember { getPlatform() }

    ComposeShipTheme(themeMode = themeMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) },
                    actions = {
                        LanguageSwitcher(
                            currentLanguage = selectedLanguageCode,
                            onLanguageSelected = { viewModel.setLanguage(it) }
                        )
                        ThemeSwitcher(
                            currentMode = themeMode,
                            onModeSelected = { viewModel.setThemeMode(it) }
                        )
                    }
                )
            }
        ) { paddingValues ->
            Row(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                NavigationRail {
                    if (platform.isMac) {
                        NavigationRailItem(
                            selected = currentFeature == Feature.MACOS_RELEASE,
                            onClick = { viewModel.selectFeature(Feature.MACOS_RELEASE) },
                            icon = {
                                Icon(
                                    Icons.Default.Laptop,
                                    contentDescription = null
                                )
                            },
                            label = { Text("macOS") }
                        )
                    }
                    NavigationRailItem(
                        selected = currentFeature == Feature.CLOUD_RUN_DEPLOY,
                        onClick = { viewModel.selectFeature(Feature.CLOUD_RUN_DEPLOY) },
                        icon = {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null
                            )
                        },
                        label = { Text("Cloud Run") }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    content(Modifier)
                }
            }
        }
    }
}

@Composable
private fun LanguageSwitcher(
    currentLanguage: String?,
    onLanguageSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf(
        null to "System",
        "en" to "English",
        "uk" to "Українська"
    )

    Box {
        TextButton(onClick = { expanded = true }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = "Switch Language"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(currentLanguage?.uppercase() ?: "SYS")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    },
                    trailingIcon = {
                        if (currentLanguage == code) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected"
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSwitcher(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = when (currentMode) {
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.DARK -> Icons.Default.DarkMode
                    ThemeMode.SYSTEM -> Icons.Default.Contrast
                },
                contentDescription = "Switch Theme"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(
                            mode.name.lowercase()
                                .replaceFirstChar { it.uppercase() })
                    },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (mode) {
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.SYSTEM -> Icons.Default.Contrast
                            },
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (currentMode == mode) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected"
                            )
                        }
                    }
                )
            }
        }
    }
}
