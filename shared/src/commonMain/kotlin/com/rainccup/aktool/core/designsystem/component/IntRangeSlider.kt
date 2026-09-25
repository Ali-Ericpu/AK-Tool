package com.rainccup.aktool.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.max
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.preference.SliderPreference

@Composable
fun IntRangeSlider(
    value: Float = 10f,
    start: Int = 0,
    maxValue: Int = 99,
    description: String = "Test",
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit = { },
    onValueChangeFinished: (Int) -> Unit = { },
) {
    SliderPreference(
        value = value,
        onValueChange = onValueChange,
        title = description,
        valueText = value.roundToInt().toString(),
        valueRange = start.toFloat()..maxValue.toFloat(),
        steps = max(maxValue - start - 1, 0),
        onValueChangeFinished = { onValueChangeFinished(value.roundToInt()) },
        modifier = modifier,
    )
}
