package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.screens.ScreenScaffold
import com.anchorguard.client.ui.theme.KidTheme
import com.anchorguard.client.ui.theme.getKidThemeColorScheme

@Composable
fun ChildSettingsScreen(
    kidTheme: KidTheme = KidTheme.OCEAN_BLUE,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val colorScheme = getKidThemeColorScheme(kidTheme)
    var selectedPage by remember { mutableStateOf(0) }

    ScreenScaffold(
        title = "Child Settings",
        subtitle = "Manage restrictions for this device",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(text = "Child Settings Screen - Page $selectedPage")
        }
    }
}
