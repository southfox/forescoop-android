//
//  CustomColor.kt
//  Ventus
//
//  Created by Javier Fuchs on 2026-08-20.
//  Copyright © 2026 Mobile Patagonia. All rights reserved.
//

package org.forescoop.ventus.data.model

import androidx.compose.ui.graphics.Color

data class CustomColor(
    val info: String? = null,
    val alpha: Float = 1f,
    val red: Float = 0f,
    val green: Float = 0f,
    val blue: Float = 0f
) {
    fun toColor(): Color {
        return Color(
            red = red / 255f,
            green = green / 255f,
            blue = blue / 255f,
            alpha = alpha
        )
    }
}
