package com.anchorguard.client.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.security.SecureSessionStore

/**
 * Language selector dropdown component.
 * Persists selection to SecureSessionStore and updates app locale via Configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    onLanguageSelected: ((languageCode: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember { SecureSessionStore(context) }
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(store.getSelectedLanguage()) }

    val languages = listOf(
        "en" to "English",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "zh" to "中文",
        "ja" to "日本語",
        "ko" to "한국어",
        "ar" to "العربية",
        "hi" to "हिन्दी",
        "pt" to "Português"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLanguage,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Language") },
            trailingIcon = {
                TextButton(
                    onClick = { expanded = !expanded }
                ) {
                    Text(text = if (expanded) "▲" else "▼")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(text = name) },
                    onClick = {
                        selectedLanguage = code
                        expanded = false
                        store.setSelectedLanguage(code)
                        onLanguageSelected?.invoke(code)
                        updateAppLocale(context, code)
                    }
                )
            }
        }
    }
}

private fun updateAppLocale(context: android.content.Context, languageCode: String) {
    val locale = java.util.Locale.forLanguageTag(languageCode)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.createConfigurationContext(config)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}
