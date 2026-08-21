//
//  AnalyticsEvent.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//
package org.forescoop.ventus.analytics

import androidx.core.os.bundleOf

/**
 * A typed catalog of everything the app logs, mirroring the iOS package's
 * AnalyticsEvent so event names/params stay consistent across platforms
 * instead of being scattered as string literals across screens/view models.
 */
sealed class AnalyticsEvent(val name: String, val params: android.os.Bundle?) {
    class SpotSelected(spotId: String) :
        AnalyticsEvent("spot_selected", bundleOf("spot_id" to spotId))

    class ForecastLoadSucceeded(spotId: String) :
        AnalyticsEvent("forecast_load_succeeded", bundleOf("spot_id" to spotId))

    class ForecastLoadFailed(spotId: String, reason: String) :
        AnalyticsEvent("forecast_load_failed", bundleOf("spot_id" to spotId, "reason" to reason))

    class FavoriteToggled(spotId: String, isFavorite: Boolean) :
        AnalyticsEvent("favorite_toggled", bundleOf("spot_id" to spotId, "is_favorite" to isFavorite))

    class UnitChanged(kind: String, value: String) :
        AnalyticsEvent("unit_changed", bundleOf("kind" to kind, "value" to value))

    object LoginAttempted : AnalyticsEvent("login_attempted", null)
    object LoginSucceeded : AnalyticsEvent("login", null)
    class LoginFailed(reason: String) :
        AnalyticsEvent("login_failed", bundleOf("reason" to reason))

    class SpotSearchPerformed(query: String, resultCount: Int) :
        AnalyticsEvent(
            "spot_search_performed",
            bundleOf("query_length" to query.length, "result_count" to resultCount)
        )
}
