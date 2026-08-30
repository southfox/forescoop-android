//
//  DebugAnalyticsLogger.kt
//  Ventus
//

package org.forescoop.ventus.analytics

import android.os.Bundle
import android.util.Log

class DebugAnalyticsLogger : AnalyticsLogging {
    override fun logScreenView(screenName: String) {
        Log.d("Analytics", "Screen View: $screenName")
    }

    override fun logEvent(name: String, params: Bundle?) {
        Log.d("Analytics", "Event: $name, Params: $params")
    }

    override fun log(message: String) {
        Log.d("Analytics", "Log: $message")
    }

    override fun recordException(throwable: Throwable) {
        Log.e("Analytics", "Exception recorded", throwable)
    }
}
