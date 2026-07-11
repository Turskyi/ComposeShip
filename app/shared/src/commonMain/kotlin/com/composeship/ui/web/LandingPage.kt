package com.composeship.ui.web

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LandingPage(
    onNavigateToSupport: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text(
            text = "ComposeShip",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "The Ultimate Developer Utility Suite for Compose Multiplatform",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Streamline your workflow with tools designed specifically for the KMP ecosystem.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Featured: macOS Release Tool",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Go from build output to App Store Connect in minutes. Automated signing, notarization, and upload.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {}) {
                Text("Download for macOS")
            }
            OutlinedButton(onClick = {}) {
                Text("View on GitHub")
            }
        }
        
        Divider()
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateToSupport) {
                Text("Support")
            }
            Text(" • ")
            TextButton(onClick = onNavigateToPrivacy) {
                Text("Privacy Policy")
            }
        }
    }
}
