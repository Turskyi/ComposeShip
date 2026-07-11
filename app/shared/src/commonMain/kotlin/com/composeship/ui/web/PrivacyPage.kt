package com.composeship.ui.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPage(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        Card(modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Overview", style = MaterialTheme.typography.titleMedium)
                Text(
                    "ComposeShip is committed to protecting your privacy. This policy explains how we handle your data.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text("Data Collection", style = MaterialTheme.typography.titleMedium)
                Text(
                    "• On Web and Android: We use Firebase Analytics to collect anonymous usage data to help us improve the app.\n" +
                    "• On iOS and macOS: No analytics are collected.\n" +
                    "• Personal Data: We do not collect or store any personal identification information on our servers.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text("Local Storage", style = MaterialTheme.typography.titleMedium)
                Text(
                    "• We use local storage (Shared Preferences/Multiplatform Settings) to save your preferences and app state.\n" +
                    "• On macOS, sensitive credentials (like App Store Connect API keys) are stored securely in the system Keychain.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text("Third-Party Services", style = MaterialTheme.typography.titleMedium)
                Text(
                    "• Firebase (Google Inc.): Used for analytics on specific platforms.\n" +
                    "• Apple services: The macOS Release tool interacts directly with Apple's servers for signing and notarization.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text("Contact", style = MaterialTheme.typography.titleMedium)
                Text(
                    "If you have any questions, contact us at dmytro@turskyi.com",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Button(onClick = onBack) {
            Text("Back to Home")
        }
    }
}
