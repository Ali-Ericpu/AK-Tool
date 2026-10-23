package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun Modifier.roundedBackground(
    color: Color = MiuixTheme.colorScheme.background.copy(alpha = 0.95f),
    shape: Shape = RoundedCornerShape(16.dp)
) = clip(shape).background(color)