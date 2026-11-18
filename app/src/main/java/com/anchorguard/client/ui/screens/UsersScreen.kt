package com.anchorguard.client.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.components.AnchorInfoCard

@Composable
fun UsersScreen() {
    ScreenScaffold(
        title = "Users",
        subtitle = "Manage child profiles and optional per-user PINs."
    ) {
        AnchorInfoCard(title = "Profiles", body = "Add, edit, or switch user profiles placeholder.")
        Spacer(Modifier.height(12.dp))
        AnchorInfoCard(title = "Profile PINs", body = "Optional per-user PIN configuration placeholder.")
    }
}
