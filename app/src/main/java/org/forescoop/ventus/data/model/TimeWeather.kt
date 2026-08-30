//
//  TimeWeather.kt
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
import java.lang.reflect.Type

@JsonAdapter(TimeWeatherDeserializer::class)
class TimeWeather(
    val info: Map<String, Any> = emptyMap()
) {
    val orderedKeys: List<String> by lazy {
        info.keys.filter { it.toIntOrNull() != null }
            .sortedBy { it.toIntOrNull() ?: 0 }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> value(hh: String?): T? {
        if (hh == null) return null
        val v = info[hh] ?: return null
        
        val result: T? = try {
            v as? T
        } catch (e: Exception) {
            null
        } ?: run {
            val str = v.toString()
            val d = str.toDoubleOrNull()
            if (d != null) {
                try {
                    d as? T
                } catch (e: Exception) {
                    try {
                        d.toInt() as? T
                    } catch (e2: Exception) {
                        str as? T
                    }
                }
            } else {
                str as? T
            }
        }
        return result
    }

    override fun toString(): String {
        return "TimeWeather: " + orderedKeys.joinToString(", ") { "$it : ${info[it] ?: "-"}" }
    }
}

class TimeWeatherDeserializer : JsonDeserializer<TimeWeather> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TimeWeather {
        val info = mutableMapOf<String, Any>()
        if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            for (entry in jsonObject.entrySet()) {
                val value = entry.value
                when {
                    value.isJsonPrimitive -> {
                        val primitive = value.asJsonPrimitive
                        when {
                            primitive.isNumber -> info[entry.key] = primitive.asDouble
                            primitive.isBoolean -> info[entry.key] = primitive.asBoolean
                            primitive.isString -> info[entry.key] = primitive.asString
                        }
                    }
                    // Handle other types if necessary, though Windguru mostly uses primitives here
                }
            }
        }
        return TimeWeather(info)
    }
}
