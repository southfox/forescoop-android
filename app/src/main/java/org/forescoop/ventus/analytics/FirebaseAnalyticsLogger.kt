//
//  FirebaseAnalyticsLogger.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Thin wrapper around Firebase Analytics/Crashlytics, mirroring the iOS
 * app's VentusFirebase. Both SDKs auto-initialize from google-services.json
 * without any explicit "configure" step, and Firebase already assigns a
 * stable per-install identifier (App Instance ID / Installation ID)
 * automatically, so there's no custom device-UUID plumbing here the way
 * there is on iOS.
 *
 * There is no static/singleton accessor here on purpose: [VentusApplication]
 * creates exactly one instance and hands it down explicitly through
 * ViewModel constructors, the same composition-root pattern the iOS app
 * uses for VentusFirebase.
 */
class FirebaseAnalyticsLogger(context: Context) : AnalyticsLogging {
    private val analytics = FirebaseAnalytics.getInstance(context.applicationContext)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun logScreenView(screenName: String) {
        log("screen: $screenName")
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        })
    }

    override fun logEvent(name: String, params: Bundle?) {
        analytics.logEvent(name, params)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
