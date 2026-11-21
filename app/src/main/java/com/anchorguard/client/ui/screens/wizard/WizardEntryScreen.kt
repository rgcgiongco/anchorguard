package com.anchorguard.client.ui.screens.wizard

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.anchorguard.client.ui.components.LanguageSelector
import com.anchorguard.client.ui.components.ThemeSelector
import com.anchorguard.client.ui.screens.ScreenScaffold
import com.anchorguard.client.ui.theme.KidTheme

/**
 * Unified wizard entry screen that replaces Welcome + Role Selection.
 *
 * Branches into three explicit paths:
 * - Path A: Parent device → sign in / create account → PIN → dashboard
 * - Path B: Child device → sign in → device authorization → permissions → general setup → child home
 * - Path C: Shared device (Parent + Child) → permissions → pairing → safety → dashboard
 */
@Composable
fun WizardEntryScreen(
    onParentDevice: () -> Unit,
    onChildDevice: () -> Unit,
    onBothDevices: () -> Unit
) {
    val context = LocalContext.current
    val store = remember { SecureSessionStore(context) }
    var kidTheme by remember { mutableStateOf(KidTheme.valueOf(store.getSelectedTheme().uppercase())) }

    ScreenScaffold(
        title = "Set up this device",
        subtitle = "Who is this device for?",
        actions = {
            LanguageSelector()
            Spacer(modifier = Modifier.width(12.dp))
            ThemeSelector(
                onThemeSelected = { theme ->
                    kidTheme = theme
                }
            )
        }
    ) {
        ThemeSelector(
            onThemeSelected = { theme ->
                kidTheme = theme
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        AnchorInfoCard(
            title = "I'm a parent — sign in or create account",
            body = "Manage family settings, review devices, and control pairing."
        )
        Spacer(modifier = Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as parent", onClick = {
            store.setWizardRole("parent")
            store.setSetupMode("parent")
            onParentDevice()
        })

        Spacer(modifier = Modifier.height(24.dp))

        AnchorInfoCard(
            title = "This is a child's device",
            body = "Apply allowed apps, schedules, and screen-time rules on this device."
        )
        Spacer(modifier = Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as child", onClick = {
            store.setWizardRole("child")
            store.setSetupMode("child_device")
            onChildDevice()
        })

        Spacer(modifier = Modifier.height(24.dp))

        AnchorInfoCard(
            title = "This device is shared — Parent + Child",
            body = "Parent has full access; Child Mode restricts apps and schedules. Switch between modes with Parent PIN."
        )
        Spacer(modifier = Modifier.height(12.dp))
        AnchorPrimaryButton(text = "Set up as shared", onClick = {
            store.setDeviceRole("both")
            store.setSetupMode("shared")
            store.setActiveMode("parent")
            onBothDevices()
        })
    }
}
