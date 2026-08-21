//
//  SettingsScreen.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.forescoop.ventus.VentusApplication
import org.forescoop.ventus.ui.common.LambdaViewModelFactory
import org.forescoop.ventus.ui.common.TemperatureUnit
import org.forescoop.ventus.ui.common.WindSpeedUnit

@Composable
private fun rememberSettingsViewModel(): SettingsViewModel {
    val application = LocalContext.current.applicationContext as VentusApplication
    return viewModel(factory = LambdaViewModelFactory { SettingsViewModel(application, application.analytics) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = rememberSettingsViewModel(),
    onNavigateBack: () -> Unit
) {
    val windUnit by viewModel.windUnit.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Wind Speed Unit", style = MaterialTheme.typography.titleMedium)
            UnitSelector(
                options = WindSpeedUnit.entries,
                selectedOption = windUnit,
                onOptionSelected = { viewModel.setWindUnit(it) },
                labelProvider = { it.label }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Temperature Unit", style = MaterialTheme.typography.titleMedium)
            UnitSelector(
                options = TemperatureUnit.entries,
                selectedOption = tempUnit,
                onOptionSelected = { viewModel.setTempUnit(it) },
                labelProvider = { it.label }
            )
        }
    }
}

@Composable
fun <T> UnitSelector(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelProvider: (T) -> String
) {
    Column(Modifier.selectableGroup()) {
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = { onOptionSelected(option) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = labelProvider(option),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
