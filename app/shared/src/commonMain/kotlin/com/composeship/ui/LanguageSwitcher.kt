package com.composeship.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.language_en
import composeship.core.generated.resources.language_system
import composeship.core.generated.resources.language_uk
import composeship.core.generated.resources.switch_language
import org.jetbrains.compose.resources.stringResource

@Composable
fun LanguageSwitcher(
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
