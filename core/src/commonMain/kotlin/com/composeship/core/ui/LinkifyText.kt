package com.composeship.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

@Composable
fun LinkifyText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val urlPattern = Regex("(https?://[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/[^\\s)]*)?)")
    val annotatedString = buildAnnotatedString {
        var lastMatchEnd = 0
        urlPattern.findAll(text).forEach { matchResult ->
            append(text.substring(lastMatchEnd, matchResult.range.first))
            val url = matchResult.value
            
            withLink(
                LinkAnnotation.Url(
                    url = url,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                )
            ) {
                append(url)
            }
            lastMatchEnd = matchResult.range.last + 1
        }
        append(text.substring(lastMatchEnd))
    }

    Text(
        text = annotatedString,
        modifier = modifier
    )
}
