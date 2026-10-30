package com.composeship.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.MainViewModel
import com.composeship.app.shared.generated.resources.Res as SharedRes
import com.composeship.app.shared.generated.resources.base64_title
import com.composeship.app.shared.generated.resources.password_generator_title
import com.composeship.core.domain.model.Feature
import com.composeship.core.domain.platform.PlatformType
import com.composeship.core.domain.platform.getPlatform
import com.composeship.core.theme.ComposeShipTheme
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.feature_cloud_run
import composeship.core.generated.resources.feature_firebase
import composeship.core.generated.resources.feature_macos
import composeship.core.generated.resources.feature_wear
import composeship.core.generated.resources.open_navigation_menu
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
                                MoreOptionsMenu(viewModel = viewModel)
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
                                        label = {
                                            Text(
                                                entry.label,
                                                modifier = if (entry.feature == Feature.PASSWORD_GENERATOR) {
                                                    Modifier.padding(horizontal = 12.dp)
                                                } else {
                                                    Modifier
                                                }
                                            )
                                        }
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
