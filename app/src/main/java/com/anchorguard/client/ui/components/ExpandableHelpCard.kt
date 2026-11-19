package com.anchorguard.client.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.unit.dp

/**
 * Expandable help card for guided wizard UX.
 *
 * Shows a title with a chevron; tapping expands the card to reveal help content
 * (e.g. "Why this permission matters"). Starts collapsed by default.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableHelpCard(
    title: String,
    content: @Composable () -> Unit,
    initiallyExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) ExpandLess else ExpandMore,
                    contentDescription = if (expanded) "Collapse help" else "Expand help",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = 0f) + slideIn(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    content()
                }
            }
        }
    }
}
