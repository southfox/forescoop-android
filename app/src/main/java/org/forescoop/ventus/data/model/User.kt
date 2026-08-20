//
//  User.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

@JsonAdapter(UserDeserializer::class)
data class User(
    @SerializedName("id_user") val idUser: Int = 0,
    @SerializedName("username") val username: String? = null,
    @SerializedName("id_country") val idCountry: Int = 0,
    @SerializedName("wind_units") val windUnits: String? = null,
    @SerializedName("temp_units") val tempUnits: String? = null,
    @SerializedName("wave_units") val waveUnits: String? = null,
    @SerializedName("pro") val pro: Int = 0,
    @SerializedName("no_ads") val noAds: Int = 0,
    @SerializedName("view_hours_from") val viewHoursFrom: Int = 0,
    @SerializedName("view_hours_to") val viewHoursTo: Int = 0,
    @SerializedName("temp_limit") val tempLimit: Int = 0,
    @SerializedName("wind_rating_limits") val windRatingLimits: List<Float> = emptyList(),
    val customColors: Map<String, List<CustomColor>>? = null
) {
    val isPro: Boolean get() = pro != 0
    val isAnonymous: Boolean get() = username.isNullOrEmpty()
}

class UserDeserializer : JsonDeserializer<User> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): User {
        val jsonObject = json.asJsonObject
        
        val idUser = jsonObject.get("id_user")?.asInt ?: 0
        val username = jsonObject.get("username")?.asString
        val idCountry = jsonObject.get("id_country")?.asInt ?: 0
        val windUnits = jsonObject.get("wind_units")?.asString
        val tempUnits = jsonObject.get("temp_units")?.asString
        val waveUnits = jsonObject.get("wave_units")?.asString
        val pro = jsonObject.get("pro")?.asInt ?: 0
        val noAds = jsonObject.get("no_ads")?.asInt ?: 0
        val viewHoursFrom = jsonObject.get("view_hours_from")?.asInt ?: 0
        val viewHoursTo = jsonObject.get("view_hours_to")?.asInt ?: 0
        val tempLimit = jsonObject.get("temp_limit")?.asInt ?: 0
        
        val windRatingLimits = mutableListOf<Float>()
        jsonObject.get("wind_rating_limits")?.asJsonArray?.forEach {
            windRatingLimits.add(it.asFloat)
        }
        
        val customColors = mutableMapOf<String, List<CustomColor>>()
        jsonObject.getAsJsonObject("colors")?.entrySet()?.forEach { entry ->
            val colorList = mutableListOf<CustomColor>()
            entry.value.asJsonArray.forEach { element ->
                val values = element.asJsonArray
                if (values.size() >= 4) {
                    colorList.add(CustomColor(
                        info = values.get(0).asString,
                        red = values.get(1).asFloat,
                        green = values.get(2).asFloat,
                        blue = values.get(3).asFloat,
                        alpha = if (values.size() > 4) values.get(4).asFloat else 1f
                    ))
                }
            }
            customColors[entry.key] = colorList
        }
        
        return User(
            idUser, username, idCountry, windUnits, tempUnits, waveUnits,
            pro, noAds, viewHoursFrom, viewHoursTo, tempLimit,
            windRatingLimits, customColors
        )
    }
}
