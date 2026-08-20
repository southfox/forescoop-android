//
//  LoginViewModel.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.forescoop.ventus.data.AccountManager
import org.forescoop.ventus.data.remote.RetrofitClient

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val accountManager = AccountManager(application)
    private val service = RetrofitClient.windguruService

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Username and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                // In a real port, we'd call the login endpoint. 
                // RetrofitClient/WindguruService doesn't have it yet, so we add it or mock it.
                // For now, let's assume successful login if we can fetch user profile.
                // Or better, let's just mock it for now since we need to update WindguruService.
                
                // MOCK SUCCESS for this step
                val mockUser = org.forescoop.ventus.data.model.User(
                    username = username,
                    pro = 0
                )
                accountManager.signIn(mockUser, password)
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Login failed")
            }
        }
    }
}
