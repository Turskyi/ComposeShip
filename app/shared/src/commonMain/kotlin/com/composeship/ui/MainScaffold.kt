package com.composeship.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.MainViewModel
import com.composeship.core.theme.ComposeShipTheme
import com.composeship.core.theme.ThemeMode
import composeship.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    viewModel: MainViewModel,
    content: @Composable (Modifier) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val selectedLanguageCode by viewModel.selectedLanguageCode.collectAsStateWithLifecycle()

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
            Box(modifier = Modifier.padding(paddingValues)) {
                content(Modifier)
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
                Icon(Icons.Default.Language, contentDescription = "Switch Language")
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
                            Icon(Icons.Default.Check, contentDescription = "Selected")
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
                    text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
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
                            Icon(Icons.Default.Check, contentDescription = "Selected")
                        }
                    }
                )
            }
        }
    }
}
