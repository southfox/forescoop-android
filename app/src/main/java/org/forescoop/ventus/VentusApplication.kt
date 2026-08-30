//
//  VentusApplication.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.forescoop.ventus.analytics.AnalyticsLogging
import org.forescoop.ventus.analytics.DebugAnalyticsLogger
import org.forescoop.ventus.data.AccountManager
import org.forescoop.ventus.data.local.PreferenceStore
import org.forescoop.ventus.data.remote.RetrofitClient
import org.forescoop.ventus.data.remote.WindguruService

class VentusApplication : Application() {
    val analytics: AnalyticsLogging by lazy { DebugAnalyticsLogger() }
    val windguruService: WindguruService by lazy { RetrofitClient().windguruService }
    val accountManager: AccountManager by lazy { AccountManager.getInstance(this, windguruService) }

    override fun onCreate() {
        super.onCreate()
        
        // Apply settings on startup
        val prefs = PreferenceStore(this)
        MainScope().launch {
            val mode = prefs.getString(PreferenceStore.KEY_APPEARANCE_MODE).first()
            when(mode) {
                "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            
            val lang = prefs.getString(PreferenceStore.KEY_LANGUAGE_CODE).first()
            if (lang != null && lang != "System") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
            }
        }
    }
}
