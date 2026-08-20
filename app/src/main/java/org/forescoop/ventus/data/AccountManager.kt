//
//  AccountManager.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.data.local.SecureCredentialStore
import org.forescoop.ventus.data.model.User

class AccountManager(context: Context) {
    private val preferenceStore = PreferenceStore(context)
    private val secureStore = SecureCredentialStore(context)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro

    suspend fun initialize() {
        val username = preferenceStore.getString("active_username").first()
        if (username != null) {
            val password = secureStore.getPassword(username)
            if (password != null) {
                // In a real app, we might verify or fetch profile here
                _isPro.value = preferenceStore.getString("is_pro").first() == "true"
            }
        }
    }

    suspend fun signIn(user: User, password: String) {
        _currentUser.value = user
        _isPro.value = user.isPro
        user.username?.let {
            preferenceStore.saveString("active_username", it)
            secureStore.savePassword(it, password)
            preferenceStore.saveString("is_pro", if (user.isPro) "true" else "false")
        }
    }

    suspend fun signOut() {
        val username = preferenceStore.getString("active_username").first()
        username?.let { secureStore.removePassword(it) }
        preferenceStore.saveString("active_username", "")
        preferenceStore.saveString("is_pro", "false")
        _currentUser.value = null
        _isPro.value = false
    }

    fun getPassword(): suspend () -> String? = {
        val username = preferenceStore.getString("active_username").first()
        username?.let { secureStore.getPassword(it) }
    }
    
    suspend fun getUsername(): String? = preferenceStore.getString("active_username").first()
}
