package com.anchorguard.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anchorguard.client.ui.theme.AnchorColors
import com.anchorguard.client.ui.theme.AnchorElevation
import com.anchorguard.client.ui.theme.AnchorShapes

enum class AnchorButtonVariant { Filled, Outlined, Text }

@Composable
fun AnchorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: AnchorButtonVariant = AnchorButtonVariant.Filled,
    destructive: Boolean = false
) {
    val danger = AnchorColors.danger
    when (variant) {
        AnchorButtonVariant.Filled -> Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors = if (destructive) ButtonDefaults.buttonColors(containerColor = danger) else ButtonDefaults.buttonColors(),
            shape = AnchorShapes.medium
        ) { Text(text) }

        AnchorButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            colors = if (destructive) ButtonDefaults.outlinedButtonColors(contentColor = danger) else ButtonDefaults.outlinedButtonColors(),
            shape = AnchorShapes.medium
        ) { Text(text) }

        AnchorButtonVariant.Text -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = if (destructive) ButtonDefaults.textButtonColors(contentColor = danger) else ButtonDefaults.textButtonColors()
        ) { Text(text) }
    }
}

@Composable
fun AnchorPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = AnchorButton(text = text, onClick = onClick, modifier = modifier, enabled = enabled, variant = AnchorButtonVariant.Filled)

@Composable
fun AnchorSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = AnchorButton(text = text, onClick = onClick, modifier = modifier, enabled = enabled, variant = AnchorButtonVariant.Outlined)

@Composable
fun AnchorCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    body: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AnchorShapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AnchorElevation.level1)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (title != null) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
            if (body != null) {
                Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content?.invoke()
        }
    }
}

@Composable
fun AnchorInfoCard(title: String, body: String, modifier: Modifier = Modifier) =
    AnchorCard(modifier = modifier, title = title, body = body)

@Composable
fun AnchorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    supportingText: String? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = trailingIcon,
            isError = isError,
            enabled = enabled,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            shape = AnchorShapes.medium
        )
        val helper = when {
            isError && !errorText.isNullOrBlank() -> errorText
            !supportingText.isNullOrBlank() -> supportingText
            else -> null
        }
        if (helper != null) {
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun AnchorSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (!description.isNullOrBlank()) {
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
fun AnchorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        shape = AnchorShapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun AnchorListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val item = @Composable {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = subtitle?.let { { Text(it) } },
            leadingContent = leading,
            trailingContent = trailing,
            modifier = modifier,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
    if (onClick != null) TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { item() } else item()
}

data class AnchorNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

@Composable
fun AnchorBottomNav(
    items: List<AnchorNavItem>,
    selectedRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onSelect(item.route) },
                icon = {
                    AnchorBadge(count = item.badgeCount) {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnchorTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = { navigationIcon?.invoke() },
        actions = { actions?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

enum class AnchorSnackbarTone { Success, Error, Warning, Info }

@Composable
fun AnchorSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    tone: AnchorSnackbarTone = AnchorSnackbarTone.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val (container, content) = when (tone) {
        AnchorSnackbarTone.Success -> AnchorColors.success to Color.White
        AnchorSnackbarTone.Error -> AnchorColors.error to AnchorColors.onError
        AnchorSnackbarTone.Warning -> AnchorColors.warning to Color(0xFF2B1E07)
        AnchorSnackbarTone.Info -> MaterialTheme.colorScheme.inverseSurface to MaterialTheme.colorScheme.inverseOnSurface
    }
    Snackbar(
        modifier = modifier.padding(8.dp),
        containerColor = container,
        contentColor = content,
        action = if (actionLabel != null && onAction != null) {
            { TextButton(onClick = onAction) { Text(actionLabel, color = content) } }
        } else null
    ) { Text(message) }
}

@Composable
fun rememberAnchorSnackbarHostState(): SnackbarHostState = remember { SnackbarHostState() }

@Composable
fun AnchorSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier)
}

enum class AnchorDialogType { Confirm, Destructive, Alert }

@Composable
fun AnchorDialog(
    open: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    type: AnchorDialogType = AnchorDialogType.Confirm
) {
    if (!open) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            val destructive = type == AnchorDialogType.Destructive
            if (destructive) {
                AnchorButton(confirmText, onConfirm, modifier = Modifier, variant = AnchorButtonVariant.Text, destructive = true)
            } else {
                TextButton(onClick = onConfirm) { Text(confirmText) }
            }
        },
        dismissButton = {
            if (type != AnchorDialogType.Alert) {
                TextButton(onClick = onDismiss) { Text(dismissText) }
            }
        },
        shape = AnchorShapes.large
    )
}

@Composable
fun AnchorProgress(
    modifier: Modifier = Modifier,
    linear: Boolean = false,
    progress: Float? = null
) {
    if (linear) {
        if (progress == null) LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        else LinearProgressIndicator(progress = { progress }, modifier = modifier.fillMaxWidth())
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            if (progress == null) CircularProgressIndicator()
            else CircularProgressIndicator(progress = { progress })
        }
    }
}

@Composable
fun AnchorBadge(
    count: Int?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (count == null || count <= 0) {
        Box(modifier = modifier) { content() }
    } else {
        BadgedBox(
            modifier = modifier,
            badge = { Badge { Text(if (count > 99) "99+" else count.toString()) } }
        ) { content() }
    }
}

@Composable
fun AnchorSectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
}
