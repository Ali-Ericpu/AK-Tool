package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun IntRangeSlider(
    title: String,
    value: Int,
    maxValue: Int,
    modifier: Modifier = Modifier,
    start: Int = 0,
    background: Color = MiuixTheme.colorScheme.background.copy(alpha = 0.95f),
    onValueChangeFinished: (Int) -> Unit = { },
) {
    var value by remember(value) { mutableFloatStateOf(value.toFloat()) }
    SliderPreference(
        value = value,
        onValueChange = { value = it },
        title = title,
        valueText = value.roundToInt().toString(),
        valueRange = start.toFloat()..maxValue.toFloat(),
        steps = max(maxValue - start - 1, 0),
        onValueChangeFinished = { onValueChangeFinished(value.roundToInt()) },
        hapticEffect = SliderDefaults.SliderHapticEffect.Step,
        modifier = modifier
            .padding(4.dp)
            .background(background, RoundedCornerShape(16.dp))
    )
}
