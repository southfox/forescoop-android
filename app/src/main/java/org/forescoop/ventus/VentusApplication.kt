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

/**
 * Composition root: creates the app's single [AnalyticsLogging] instance
 * once and hands it down explicitly to ViewModels through their
 * constructors, mirroring how the iOS app's AppDelegate/App structs own a
 * single VentusFirebase instance instead of exposing a static `.shared`.
 */
class VentusApplication : Application() {
    val analytics: AnalyticsLogging by lazy { FirebaseAnalyticsLogger(this) }
}
