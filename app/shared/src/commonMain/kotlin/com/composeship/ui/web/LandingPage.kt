package com.composeship.ui.web

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeship.core.ui.TechnicalGridBackground
import com.composeship.app.shared.generated.resources.Res
import com.composeship.app.shared.generated.resources.ComposeShip_Mac_Screenshot_1_macOS_Release_2880x1800
import com.composeship.app.shared.generated.resources.ComposeShip_Mac_Screenshot_3_Cloud_Run_2880x1800
import com.composeship.app.shared.generated.resources.ComposeShip_Mac_Screenshot_4_Firebase_Hosting_2880x1800
import com.composeship.app.shared.generated.resources.ComposeShip_Mac_Screenshot_5_Password_Generator_2880x1800
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun LandingPage(
    onEnterApp: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    isWeb: Boolean = true
) {
    val uriHandler = LocalUriHandler.current

    // TODO: Add App Store and Google Play badges here once published
    val screenshots = listOf<DrawableResource>(
        Res.drawable.ComposeShip_Mac_Screenshot_1_macOS_Release_2880x1800,
        Res.drawable.ComposeShip_Mac_Screenshot_4_Firebase_Hosting_2880x1800,
        Res.drawable.ComposeShip_Mac_Screenshot_3_Cloud_Run_2880x1800,
        Res.drawable.ComposeShip_Mac_Screenshot_5_Password_Generator_2880x1800
    )

    Box(modifier = Modifier.fillMaxSize()) {
        TechnicalGridBackground()

        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = MaterialTheme.shapes.extraLarge,
            border = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) null
            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Header Title
                Text(
                    text = "ComposeShip",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = "The Ultimate Developer Utility Suite for Compose Multiplatform",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Description
                Text(
                    text = "Streamline your workflow with tools designed specifically for the KMP ecosystem, including automated macOS releases, Cloud Run deployer, Firebase Hosting, secure password generation, and local Base64 encoding.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(max = 700.dp)
                )

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onEnterApp,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Launch App / Explore Tools")
                    }
                    OutlinedButton(
                        onClick = { uriHandler.openUri("https://github.com/Turskyi/ComposeShip") },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("View on GitHub")
                    }
                }

                // Featured Capabilities Card
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f).widthIn(max = 900.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Featured Capabilities",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider()
                        FeatureItem(
                            title = "macOS Release Automation",
                            description = "Go from build output to App Store Connect in minutes. Automated signing, notarization, and upload."
                        )
                        FeatureItem(
                            title = "Cloud Run & Firebase Hosting",
                            description = "Easily deploy your WASM web applications and containers to Google Cloud Run and Firebase Hosting."
                        )
                        FeatureItem(
                            title = "Security & Utility Tools",
                            description = "Generate robust passwords and encode files locally to Base64 without uploading any data to servers."
                        )
                    }
                }

                // Screenshots Carousel / Gallery
                if (isWeb) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "App Screenshots",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        ) {
                            items(screenshots) { screenshot ->
                                Card(
                                    modifier = Modifier.width(420.dp).fillMaxHeight(),
                                    shape = MaterialTheme.shapes.medium,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Image(
                                        painter = painterResource(screenshot),
                                        contentDescription = "ComposeShip Screenshot",
                                        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(modifier = Modifier.fillMaxWidth(0.8f))

                // Footer Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNavigateToSupport) {
                        Text("Support")
                    }
                    Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onNavigateToPrivacy) {
                        Text("Privacy Policy")
                    }
                    Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { uriHandler.openUri("https://github.com/Turskyi/ComposeShip") }) {
                        Text("GitHub Source")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
