package com.anchorguard.client.ui.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.anchorguard.client.enforcement.AnchorGuardAccessibilityService
import com.anchorguard.client.enforcement.AnchorGuardDeviceAdminReceiver
import com.anchorguard.client.enforcement.AnchorGuardService
import com.anchorguard.client.enforcement.EnforcementController
import com.anchorguard.client.enforcement.EnforcementState
import com.anchorguard.client.ui.components.AnchorInfoCard
import com.anchorguard.client.ui.components.AnchorPrimaryButton
import com.anchorguard.client.ui.components.AnchorSecondaryButton
import androidx.compose.runtime.collectAsState

@Composable
fun SecurityScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by EnforcementController.state.collectAsState()
    var deviceAdminActive by remember { mutableStateOf(isDeviceAdminActive(context)) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var usageGranted by remember { mutableStateOf(hasUsageAccess(context)) }
    var accessibilityEnabled by remember { mutableStateOf(isAccessibilityEnabled(context)) }
    var ignoringBattery by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                deviceAdminActive = isDeviceAdminActive(context)
                overlayGranted = Settings.canDrawOverlays(context)
                usageGranted = hasUsageAccess(context)
                accessibilityEnabled = isAccessibilityEnabled(context)
                ignoringBattery = isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    ScreenScaffold(
        title = "Security",
        subtitle = "Device protection status and enforcement controls (Module 04)."
    ) {
        AnchorInfoCard(
            title = "Enforcement service",
            body = "State: ${stateLabel(state)}. Foreground watchdog, overlay blocker, and boot persistence are wired in this module."
        )
        Spacer(Modifier.height(8.dp))
        AnchorPrimaryButton(
            text = if (state == EnforcementState.IDLE) "Start protection service" else "Refresh protection service",
            onClick = { AnchorGuardService.start(context) }
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(
            text = "Stop protection service",
            onClick = { EnforcementController.stop(context) },
            enabled = state != EnforcementState.IDLE
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(
            text = "Exit parent mode",
            onClick = { EnforcementController.exitParentMode(context) },
            enabled = state == EnforcementState.PARENT_MODE
        )

        Spacer(Modifier.height(16.dp))
        AnchorInfoCard(
            title = "Device Admin",
            body = if (deviceAdminActive) "Active. Uninstall protection can be enforced by policy." else "Not active. Enable to support tamper proofing."
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(
            text = "Open Device Admin settings",
            onClick = {
                val cn = ComponentName(context, AnchorGuardDeviceAdminReceiver::class.java)
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "AnchorGuard uses Device Admin for device protection features.")
                }
                context.startActivity(intent)
            }
        )

        Spacer(Modifier.height(16.dp))
        AnchorInfoCard(
            title = "Display over other apps",
            body = if (overlayGranted) "Granted. Block overlay can be shown." else "Not granted. Required to show the block screen."
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(
            text = "Open overlay permission",
            onClick = {
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
            }
        )

        Spacer(Modifier.height(16.dp))
        AnchorInfoCard(
            title = "Usage access",
            body = if (usageGranted) "Granted. Foreground app detection is available." else "Not granted. Required for watchdog app blocking."
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(
            text = "Open usage access",
            onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        )

        Spacer(Modifier.height(16.dp))
        AnchorInfoCard(
            title = "Accessibility service",
            body = if (accessibilityEnabled) "Enabled. Event-driven app change detection available." else "Not enabled (optional). Polling watchdog is used; enable for event-driven detection."
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(
            text = "Open accessibility settings",
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        )

        Spacer(Modifier.height(16.dp))
        AnchorInfoCard(
            title = "Battery optimization",
            body = if (ignoringBattery) "Unrestricted. Service is less likely to be stopped." else "Optimized. Consider allowing unrestricted battery usage for reliability."
        )
        Spacer(Modifier.height(8.dp))
        AnchorSecondaryButton(
            text = "Request battery optimization exception",
            onClick = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            },
            enabled = !ignoringBattery
        )

        Spacer(Modifier.height(12.dp))
        Text("Package observation and boot persistence receivers are declared and will restart/refresh enforcement on relevant events.")
    }
}

private fun stateLabel(state: EnforcementState): String = when (state) {
    EnforcementState.IDLE -> "Idle"
    EnforcementState.RUNNING -> "Running"
    EnforcementState.BLOCKED -> "Blocked"
    EnforcementState.PARENT_MODE -> "Parent mode"
}

private fun isDeviceAdminActive(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val cn = ComponentName(context, AnchorGuardDeviceAdminReceiver::class.java)
    return dpm.isAdminActive(cn)
}

private fun hasUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    }
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

private fun isAccessibilityEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabled = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    val target = ComponentName(context, AnchorGuardAccessibilityService::class.java).flattenToString()
    return enabled.any { it.id == target || it.id.startsWith(context.packageName + "/") }
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}
