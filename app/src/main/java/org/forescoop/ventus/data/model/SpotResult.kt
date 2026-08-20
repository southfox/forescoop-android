//
//  SpotResult.kt
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

@JsonAdapter(SpotResultDeserializer::class)
class SpotResult(
    val count: Int = 0,
    val spots: List<SpotOwner> = emptyList()
) {
    override fun toString(): String {
        return "SpotResult: $count spots, $spots"
    }
}

class SpotResultDeserializer : JsonDeserializer<SpotResult> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SpotResult {
        val jsonObject = json.asJsonObject
        val count = jsonObject.get("count")?.asInt ?: 0
        val spotsElement = jsonObject.get("spots")
        
        val spots = mutableListOf<SpotOwner>()
        if (spotsElement != null) {
            if (spotsElement.isJsonArray) {
                spotsElement.asJsonArray.forEach {
                    spots.add(context.deserialize(it, SpotOwner::class.java))
                }
            } else if (spotsElement.isJsonObject) {
                spotsElement.asJsonObject.entrySet().forEach { entry ->
                    val identifier = entry.key
                    val label = entry.value.asString
                    val parts = label.split("-").map { it.trim() }
                    if (parts.size == 2) {
                        spots.add(SpotOwner(
                            idSpot = identifier,
                            country = parts[0],
                            spotName = parts[1]
                        ))
                    }
                }
            }
        }
        
        return SpotResult(count, spots)
    }
}
