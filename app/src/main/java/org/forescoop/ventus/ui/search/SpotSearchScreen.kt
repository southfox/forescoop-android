//
//  SpotSearchScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.R
import org.forescoop.ventus.VentusApplication
import org.forescoop.ventus.data.model.SpotOwner
import org.forescoop.ventus.domain.repository.WindguruRepository
import org.forescoop.ventus.ui.common.LambdaViewModelFactory

@Composable
private fun rememberSpotSearchViewModel(): SpotSearchViewModel {
    val application = LocalContext.current.applicationContext as VentusApplication
    return viewModel(factory = LambdaViewModelFactory {
        SpotSearchViewModel(
            application,
            application.analytics,
            WindguruRepository(application.windguruService)
        )
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotSearchScreen(
    viewModel: SpotSearchViewModel = rememberSpotSearchViewModel(),
    onSpotSelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            viewModel.search(it)
                        },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = ""; viewModel.search("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    item {
                        EmptyState(stringResource(R.string.enter_city_or_spot))
                    }
                }
                is SearchUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is SearchUiState.Success -> {
                    if (state.favorites.isNotEmpty()) {
                        item {
                            SectionHeader(stringResource(R.string.favorites_header))
                        }
                        items(state.favorites) { spot ->
                            SpotRow(spot, isFavorite = true, onClick = { onSpotSelected(spot.idSpot.toString()) })
                        }
                    }
                    
                    if (state.spots.isNotEmpty()) {
                        item {
                            SectionHeader(stringResource(R.string.spots_header))
                        }
                        items(state.spots) { spot ->
                            SpotRow(spot, isFavorite = false, onClick = { onSpotSelected(spot.idSpot.toString()) })
                        }
                    } else if (searchQuery.isNotEmpty()) {
                        item {
                            EmptyState(stringResource(R.string.no_spots_found))
                        }
                    }
                }
                is SearchUiState.Error -> {
                    item {
                        EmptyState("Error: ${state.message}")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SpotRow(spot: SpotOwner, isFavorite: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(spot.spotName ?: "Unknown") },
        supportingContent = { Text(spot.country ?: "") },
        leadingContent = {
            if (isFavorite) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
            } else {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
