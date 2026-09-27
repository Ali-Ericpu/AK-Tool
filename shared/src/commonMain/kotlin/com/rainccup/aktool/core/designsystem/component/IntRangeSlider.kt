package com.rainccup.aktool.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.preference.SliderPreference
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun IntRangeSlider(
    value: Int,
    maxValue: Int,
    modifier: Modifier = Modifier,
    start: Int = 0,
    description: String = "Test",
    onValueChangeFinished: (Int) -> Unit = { },
) {
    var value by remember(value) { mutableFloatStateOf(value.toFloat()) }
    SliderPreference(
        value = value,
        onValueChange = { value = it },
        title = description,
        valueText = value.roundToInt().toString(),
        valueRange = start.toFloat()..maxValue.toFloat(),
        steps = max(maxValue - start - 1, 0),
        onValueChangeFinished = { onValueChangeFinished(value.roundToInt()) },
        modifier = modifier,
    )
}
