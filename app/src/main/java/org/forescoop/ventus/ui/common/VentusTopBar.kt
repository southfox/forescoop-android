//
//  VentusTopBar.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.forescoop.ventus.R

@Composable
fun VentusTopBar(
    currentRoute: String,
    onNavigateToDashboard: () -> Unit,
    onNavigateToGrid: () -> Unit,
    onNavigateToGraph: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCurrentLocationClick: () -> Unit,
    isFavorite: Boolean,
    isAuthenticated: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = stringResource(R.string.menu), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (currentRoute != "dashboard") {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_dashboard)) },
                            onClick = { showMenu = false; onNavigateToDashboard() },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ViewQuilt, contentDescription = null) }
                        )
                    }
                    if (currentRoute != "grid") {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_grid)) },
                            onClick = { showMenu = false; onNavigateToGrid() },
                            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) }
                        )
                    }
                    if (currentRoute != "graph") {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_graph)) },
                            onClick = { showMenu = false; onNavigateToGraph() },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null) }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_settings)) },
                        onClick = { showMenu = false; onNavigateToSettings() },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_help)) },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_about)) },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                    HorizontalDivider()
                    if (isAuthenticated) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_profile)) },
                            onClick = { showMenu = false; onNavigateToProfile() },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_logout), color = Color.Red) },
                            onClick = { showMenu = false; onLogout() },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_login)) },
                            onClick = { showMenu = false; onNavigateToLogin() },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) }
                        )
                    }
                }
            }

            IconButton(
                onClick = onNavigateToSearch,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.search_placeholder), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(
                onClick = onCurrentLocationClick,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = stringResource(R.string.current_location), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(
                onClick = onToggleFavorite,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = stringResource(R.string.favorite),
                    tint = if (isFavorite) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
