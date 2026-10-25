package com.composeship.ui.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.composeship.core.ui.TechnicalGridBackground
import com.composeship.app.shared.generated.resources.Res
import com.composeship.app.shared.generated.resources.password_begin_with_letter
import com.composeship.app.shared.generated.resources.password_copy_all
import com.composeship.app.shared.generated.resources.password_copy_one
import com.composeship.app.shared.generated.resources.password_exclude_duplicates
import com.composeship.app.shared.generated.resources.password_exclude_sequential
import com.composeship.app.shared.generated.resources.password_exclude_similar
import com.composeship.app.shared.generated.resources.password_generate
import com.composeship.app.shared.generated.resources.password_generated
import com.composeship.app.shared.generated.resources.password_generator_title
import com.composeship.app.shared.generated.resources.password_include_lowercase
import com.composeship.app.shared.generated.resources.password_include_numbers
import com.composeship.app.shared.generated.resources.password_include_uppercase
import com.composeship.app.shared.generated.resources.password_length
import com.composeship.app.shared.generated.resources.password_quantity
import com.composeship.app.shared.generated.resources.password_validation
import com.composeship.password.PasswordOptions
import com.composeship.password.canGeneratePasswords
import com.composeship.password.generatePasswords
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasswordGeneratorScreen(modifier: Modifier = Modifier) {
    val clipboardManager = LocalClipboardManager.current
    var lengthText by remember { mutableStateOf("16") }
    var quantityText by remember { mutableStateOf("4") }
    var options by remember { mutableStateOf(PasswordOptions()) }
    var passwords by remember { mutableStateOf(generatePasswords()) }
    var message by remember { mutableStateOf("") }
    val copiedMessage = stringResource(Res.string.password_generated)
    val validationMessage = stringResource(Res.string.password_validation)

    Box(modifier = modifier.fillMaxSize()) {
        TechnicalGridBackground()

        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            border = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) null
            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(Res.string.password_generator_title),
                    style = MaterialTheme.typography.headlineMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = {
                            lengthText = it.filter(Char::isDigit).take(2)
                        },
                        label = { Text(stringResource(Res.string.password_length)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it.filter(Char::isDigit).take(2)
                        },
                        label = { Text(stringResource(Res.string.password_quantity)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OptionToggle(
                        stringResource(Res.string.password_include_numbers),
                        options.includeNumbers
                    ) {
                        options = options.copy(includeNumbers = it)
                    }
                    OptionToggle(
                        stringResource(Res.string.password_include_lowercase),
                        options.includeLowercase
                    ) {
                        options = options.copy(includeLowercase = it)
                    }
                    OptionToggle(
                        stringResource(Res.string.password_include_uppercase),
                        options.includeUppercase
                    ) {
                        options = options.copy(includeUppercase = it)
                    }
                    OptionToggle(
                        stringResource(Res.string.password_begin_with_letter),
                        options.beginWithLetter
                    ) {
                        options = options.copy(beginWithLetter = it)
                    }
                    OptionToggle(
                        stringResource(Res.string.password_exclude_similar),
                        options.excludeSimilarCharacters
                    ) {
                        options = options.copy(excludeSimilarCharacters = it)
                    }
                    OptionToggle(
                        stringResource(Res.string.password_exclude_duplicates),
                        options.excludeDuplicateCharacters
                    ) {
                        options = options.copy(excludeDuplicateCharacters = it)
                    }
                    OptionToggle(
                        stringResource(Res.string.password_exclude_sequential),
                        options.excludeSequentialCharacters
                    ) {
                        options = options.copy(excludeSequentialCharacters = it)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        val length = lengthText.toIntOrNull()
                        val quantity = quantityText.toIntOrNull()
                        if (length == null || quantity == null || !canGeneratePasswords(
                                length,
                                quantity,
                                options
                            )
                        ) {
                            message = validationMessage
                        } else {
                            passwords =
                                generatePasswords(length, quantity, options)
                            message = ""
                        }
                    }) {
                        Text(stringResource(Res.string.password_generate))
                    }
                    Button(onClick = {
                        clipboardManager.setText(
                            AnnotatedString(
                                passwords.joinToString(
                                    "\n"
                                )
                            )
                        )
                        message = copiedMessage
                    }) {
                        Text(stringResource(Res.string.password_copy_all))
                    }
                }

                if (message.isNotEmpty()) {
                    Text(message, color = MaterialTheme.colorScheme.primary)
                }

                passwords.forEachIndexed { index, password ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(
                                start = 16.dp,
                                end = 8.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}.",
                                modifier = Modifier.padding(end = 12.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                password,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(
                                    AnnotatedString(
                                        password
                                    )
                                )
                                message = copiedMessage
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = stringResource(Res.string.password_copy_one)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
