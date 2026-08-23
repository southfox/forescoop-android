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
import org.forescoop.ventus.data.remote.WindguruService

class AccountManager private constructor(context: Context, private val service: WindguruService) {
    private val preferenceStore = PreferenceStore(context)
    private val secureStore = SecureCredentialStore(context)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro

    suspend fun initialize() {
        val username = preferenceStore.getString("active_username").first()
        if (!username.isNullOrEmpty()) {
            val password = secureStore.getPassword(username)
            if (password != null) {
                try {
                    refreshProfile()
                } catch (e: Exception) {
                    // Fallback to cached pro status if refresh fails
                    _isPro.value = preferenceStore.getString("is_pro").first() == "true"
                }
            }
        }
    }

    suspend fun refreshProfile() {
        val username = preferenceStore.getString("active_username").first() ?: return
        val password = secureStore.getPassword(username) ?: return
        
        try {
            val user = service.login(username = username, password = password)
            _currentUser.value = user
            _isPro.value = user.isPro
            preferenceStore.saveString("is_pro", if (user.isPro) "true" else "false")
        } catch (e: Exception) {
            // Log error or handle refresh failure
            throw e
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
        if (!username.isNullOrEmpty()) {
            secureStore.removePassword(username)
        }
        preferenceStore.saveString("active_username", "")
        preferenceStore.saveString("is_pro", "false")
        _currentUser.value = null
        _isPro.value = false
    }

    fun getPassword(): suspend () -> String? = {
        val username = preferenceStore.getString("active_username").first()
        if (username.isNullOrEmpty()) null else secureStore.getPassword(username)
    }
    
    suspend fun getUsername(): String? = preferenceStore.getString("active_username").first()

    companion object {
        @Volatile
        private var instance: AccountManager? = null

        fun getInstance(context: Context, service: WindguruService): AccountManager {
            return instance ?: synchronized(this) {
                instance ?: AccountManager(context.applicationContext, service).also { instance = it }
            }
        }
    }
}
