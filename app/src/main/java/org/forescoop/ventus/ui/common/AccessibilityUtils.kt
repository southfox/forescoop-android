package org.forescoop.ventus.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

fun Modifier.forecastAccessibilityContainer(
    label: String,
    hint: String,
    identifier: String
): Modifier = this.semantics {
    contentDescription = if (hint.isNotEmpty()) "$label. $hint" else label
}.testTag(identifier)
