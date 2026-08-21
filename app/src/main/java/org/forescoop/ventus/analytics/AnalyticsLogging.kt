//
//  AnalyticsLogging.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.analytics

import android.os.Bundle

/**
 * Everything the app needs from an analytics/crash-reporting backend,
 * mirroring the iOS package's AnalyticsLogging protocol. ViewModels take a
 * conformer through their constructor instead of reaching for a static
 * singleton, so [FirebaseAnalyticsLogger] is swappable for [NoOpAnalyticsLogger]
 * in previews and tests.
 */
interface AnalyticsLogging {
    fun logEvent(name: String, params: Bundle? = null)
    fun logScreenView(screenName: String)

    /** Crashlytics breadcrumb: shows up alongside the next crash/non-fatal report. */
    fun log(message: String)
    fun recordException(throwable: Throwable)
}

fun AnalyticsLogging.log(event: AnalyticsEvent) {
    logEvent(event.name, event.params)
}

/** Drops every call on the floor. Used in previews and tests that don't care about analytics. */
class NoOpAnalyticsLogger : AnalyticsLogging {
    override fun logEvent(name: String, params: Bundle?) {}
    override fun logScreenView(screenName: String) {}
    override fun log(message: String) {}
    override fun recordException(throwable: Throwable) {}
}
