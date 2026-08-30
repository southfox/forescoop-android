//
//  ForecastPrecipitationAgreement.kt
//  Ventus
//

package org.forescoop.ventus.data.model

data class ForecastPrecipitationAgreement(
    val rainyModelCount: Int,
    val modelCount: Int
) {
    val percentage: Int
        get() = if (modelCount > 0) (rainyModelCount * 100) / modelCount else 0

    val isLow: Boolean
        get() = percentage < 50
}

fun List<ForecastModel>.precipitationAgreement(hour: String?): ForecastPrecipitationAgreement? {
    if (isEmpty()) return null
    val rainy = count { model ->
        val precip = model.info?.precipitation(hour) ?: model.info?.precipitation1(hour) ?: 0.0
        precip > 0
    }
    return ForecastPrecipitationAgreement(rainy, size)
}
