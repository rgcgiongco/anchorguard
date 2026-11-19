package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchorguard.client.auth.security.SecureSessionStore
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import com.anchorguard.client.ui.screens.ScreenScaffold

@Composable
fun WizardRoleSelectionScreen(
    onParentDevice: () -> Unit,
    onChildDevice: () -> Unit
) {
    val context = LocalContext.current
    val store = remember { SecureSessionStore(context) }

    ScreenScaffold(
        title = "Choose your role",
        subtitle = "Is this device for a parent or a child?"
    ) {
        AnchorInfoCard(
            title = "This is a Parent device",
            body = "Manage family settings, review devices, and control pairing."
        )
        Spacer(Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as parent", onClick = {
            store.setWizardRole("parent")
            onParentDevice()
        })
        Spacer(Modifier.height(20.dp))
        AnchorInfoCard(
            title = "This is a Child device",
            body = "Apply allowed apps, schedules, and screen-time rules on this device."
        )
        Spacer(Modifier.height(12.dp))
        AnchorSecondaryButton(text = "Set up as child", onClick = {
            store.setWizardRole("child")
            onChildDevice()
        })
    }
}
