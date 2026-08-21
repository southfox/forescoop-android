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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.VentusApplication
import org.forescoop.ventus.data.model.SpotOwner
import org.forescoop.ventus.ui.common.LambdaViewModelFactory

@Composable
private fun rememberSpotSearchViewModel(): SpotSearchViewModel {
    val application = LocalContext.current.applicationContext as VentusApplication
    return viewModel(factory = LambdaViewModelFactory { SpotSearchViewModel(application, application.analytics) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotSearchScreen(
    viewModel: SpotSearchViewModel = rememberSpotSearchViewModel(),
    onSpotSelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { newValue ->
                            // Allow only alphanumeric, spaces, and basic punctuation, no emoticons
                            if (newValue.all { it.isLetterOrDigit() || it.isWhitespace() || it == '-' || it == ',' }) {
                                query = newValue
                            }
                        },
                        placeholder = { Text("Search spots...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search,
                            keyboardType = KeyboardType.Uri,
                            autoCorrectEnabled = false,
                            platformImeOptions = PlatformImeOptions("allowEmoji=false")
                        ),
                        keyboardActions = KeyboardActions(onSearch = { viewModel.search(query) }),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.search(query) }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Text("Enter a city or spot", modifier = Modifier.align(Alignment.Center))
                }
                is SearchUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SearchUiState.Success -> {
                    LazyColumn {
                        if (state.favorites.isNotEmpty()) {
                            item {
                                Text("Favorites", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(16.dp))
                            }
                            items(state.favorites) { spot ->
                                SpotItem(spot = spot, isFavorite = true, onClick = { spot.idSpot?.let { onSpotSelected(it) } })
                            }
                        }
                        if (state.spots.isNotEmpty()) {
                            item {
                                Text("Spots", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(16.dp))
                            }
                            items(state.spots) { spot ->
                                SpotItem(spot = spot, isFavorite = false, onClick = { spot.idSpot?.let { onSpotSelected(it) } })
                            }
                        } else if (state.favorites.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No spots found")
                                }
                            }
                        }
                    }
                }
                is SearchUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun SpotItem(spot: SpotOwner, isFavorite: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(spot.displayName) },
        supportingContent = {
            val details = listOfNotNull(spot.spot, spot.countryName).joinToString(" · ")
            Text(details)
        },
        leadingContent = {
            if (isFavorite) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow)
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
