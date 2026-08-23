//
//  VentusApplication.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus

import android.app.Application
import org.forescoop.ventus.analytics.AnalyticsLogging
import org.forescoop.ventus.analytics.FirebaseAnalyticsLogger
import org.forescoop.ventus.data.AccountManager
import org.forescoop.ventus.data.remote.RetrofitClient
import org.forescoop.ventus.data.remote.WindguruService

class VentusApplication : Application() {
    val analytics: AnalyticsLogging by lazy { FirebaseAnalyticsLogger(this) }
    val windguruService: WindguruService by lazy { RetrofitClient().windguruService }
    val accountManager: AccountManager by lazy { AccountManager.getInstance(this, windguruService) }
}
