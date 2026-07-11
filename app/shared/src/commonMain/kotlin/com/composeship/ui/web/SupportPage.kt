package com.composeship.ui.web

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SupportPage(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Support",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Need help with ComposeShip? We're here for you.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Card(modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Email Support", style = MaterialTheme.typography.titleMedium)
                Text("dmytro@turskyi.com", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Web Support", style = MaterialTheme.typography.titleMedium)
                Text("https://turskyi.com/#/support", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Telegram Community", style = MaterialTheme.typography.titleMedium)
                Text("https://t.me/+7PclTqQfEphjYTAy", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Button(onClick = onBack) {
            Text("Back to Home")
        }
    }
}
