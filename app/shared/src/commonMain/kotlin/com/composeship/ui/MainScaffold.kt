package com.composeship.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.MainViewModel
import com.composeship.core.domain.model.Feature
import com.composeship.core.domain.platform.PlatformType
import com.composeship.core.domain.platform.getPlatform
import com.composeship.core.theme.ComposeShipTheme
import com.composeship.core.theme.ThemeMode
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.app_name
import composeship.core.generated.resources.feature_cloud_run
import composeship.core.generated.resources.feature_firebase
import composeship.core.generated.resources.feature_macos
import composeship.core.generated.resources.feature_wear
import composeship.core.generated.resources.language_en
import composeship.core.generated.resources.language_system
import composeship.core.generated.resources.language_uk
import composeship.core.generated.resources.open_navigation_menu
import composeship.core.generated.resources.switch_language
import composeship.core.generated.resources.switch_theme
import composeship.core.generated.resources.theme_dark
import composeship.core.generated.resources.theme_light
import composeship.core.generated.resources.theme_system
import com.composeship.app.shared.generated.resources.Res as SharedRes
import com.composeship.app.shared.generated.resources.base64_title
import com.composeship.app.shared.generated.resources.password_generator_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val COMPACT_LAYOUT_WIDTH_DP = 600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    viewModel: MainViewModel,
    showOnboarding: Boolean,
    content: @Composable (Modifier) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val selectedLanguageCode by viewModel.selectedLanguageCode.collectAsStateWithLifecycle()
    val currentFeature by viewModel.currentFeature.collectAsStateWithLifecycle()
    val platform = remember { getPlatform() }
    val isAndroid = platform.type == PlatformType.ANDROID
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val macEntry = if (platform.isMac) {
        NavigationEntry(Feature.MACOS_RELEASE, Icons.Default.Laptop, stringResource(Res.string.feature_macos))
    } else {
        null
    }
    val navigationEntries = listOfNotNull(
        macEntry,
        NavigationEntry(Feature.CLOUD_RUN_DEPLOY, Icons.Default.CloudUpload, stringResource(Res.string.feature_cloud_run)),
        if (isAndroid) null else NavigationEntry(Feature.FIREBASE_HOSTING, Icons.Default.Cloud, stringResource(Res.string.feature_firebase)),
        if (isAndroid) null else NavigationEntry(Feature.WEAR_RELEASE, Icons.Default.Laptop, stringResource(Res.string.feature_wear)),
        NavigationEntry(Feature.PASSWORD_GENERATOR, Icons.Default.Key, stringResource(SharedRes.string.password_generator_title)),
        NavigationEntry(Feature.FILE_TO_BASE64, Icons.Default.DataObject, stringResource(SharedRes.string.base64_title))
    )

    ComposeShipTheme(themeMode = themeMode) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val compactLayout = maxWidth < COMPACT_LAYOUT_WIDTH_DP.dp
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = compactLayout && !showOnboarding,
                drawerContent = {
                    if (compactLayout && !showOnboarding) {
                        ModalDrawerSheet {
                            navigationEntries.forEach { entry ->
                                NavigationDrawerItem(
                                    selected = currentFeature == entry.feature,
                                    onClick = {
                                        coroutineScope.launch {
                                            drawerState.close()
                                            viewModel.selectFeature(entry.feature)
                                        }
                                    },
                                    icon = { Icon(entry.icon, contentDescription = null) },
                                    label = { Text(entry.label) },
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                if (compactLayout && !showOnboarding) {
                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                        Icon(
                                            Icons.Default.Menu,
                                            contentDescription = stringResource(Res.string.open_navigation_menu)
                                        )
                                    }
                                }
                            },
                            title = { AnimatedAppName() },
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
                        if (!showOnboarding && !compactLayout) {
                            NavigationRail(
                                modifier = Modifier.zIndex(1f),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                navigationEntries.forEach { entry ->
                                    NavigationRailItem(
                                        selected = currentFeature == entry.feature,
                                        onClick = { viewModel.selectFeature(entry.feature) },
                                        icon = { Icon(entry.icon, contentDescription = entry.label) },
                                        label = { Text(entry.label) }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            content(Modifier)
                        }
                    }
                }
            }
        }
    }
}

private data class NavigationEntry(
    val feature: Feature,
    val icon: ImageVector,
    val label: String
)

@Composable
private fun AnimatedAppName() {
    val transition = rememberInfiniteTransition(label = "appNameGradient")
    val gradientOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "appNameGradientOffset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary
        ),
        start = Offset(1000f * gradientOffset, 0f),
        end = Offset(1000f * (gradientOffset + 1f), 100f)
    )

    Text(
        text = stringResource(Res.string.app_name),
        style = MaterialTheme.typography.titleLarge.copy(brush = brush)
    )
}

@Composable
private fun LanguageSwitcher(
    currentLanguage: String?,
    onLanguageSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf(
        null to stringResource(Res.string.language_system),
        "en" to stringResource(Res.string.language_en),
        "uk" to stringResource(Res.string.language_uk)
    )

    Box {
        TextButton(onClick = { expanded = true }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = stringResource(Res.string.switch_language)
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
                contentDescription = stringResource(Res.string.switch_theme)
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
                            when (mode) {
                                ThemeMode.LIGHT -> stringResource(Res.string.theme_light)
                                ThemeMode.DARK -> stringResource(Res.string.theme_dark)
                                ThemeMode.SYSTEM -> stringResource(Res.string.theme_system)
                            }
                        )
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
                            Icon(Icons.Default.Check, contentDescription = "Selected")
                        }
                    }
                )
            }
        }
    }
}
