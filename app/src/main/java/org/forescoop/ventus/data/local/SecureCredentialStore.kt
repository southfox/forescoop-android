//
//  SecureCredentialStore.kt
//  Ventus
//

package org.forescoop.ventus.data.local

import android.content.Context
import android.content.SharedPreferences

class SecureCredentialStore(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("secure_prefs_fallback", Context.MODE_PRIVATE)

    fun savePassword(username: String, password: String) {
        sharedPreferences.edit().putString(username, password).apply()
    }

    fun getPassword(username: String): String? {
        return sharedPreferences.getString(username, null)
    }

    fun removePassword(username: String) {
        sharedPreferences.edit().remove(username).apply()
    }
}
