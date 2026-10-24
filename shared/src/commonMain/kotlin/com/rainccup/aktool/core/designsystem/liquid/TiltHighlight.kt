// Adapted from Kyant0/AndroidLiquidGlass (Apache-2.0) and compose-miuix-ui's
// IosLiquidGlassNavigationBar example, via KernelSU's FloatingBottomBar port.

package com.rainccup.aktool.core.designsystem.liquid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.sensor.rememberDeviceTilt
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// 与 miuix-blur 的 HighlightStyle 内部 LIGHT_REF 对齐：光源落在 (0.5, 0.7) 时贡献为零。
private const val LIGHT_REF_X = 0.5f
private const val LIGHT_REF_Y = 0.7f

// |g_xy| > 0.1 才认为设备有明确的倾斜方向（约 6°），否则回到「默认从上方打光」。
private const val GRAVITY_DIR_THRESHOLD_SQ = 0.01f

// 角度量化到 3°，避免传感器噪声让高光每帧抖动。
private const val GRAVITY_ANGLE_STEP_RAD = (3.0 * PI / 180.0).toFloat()

/** 重力方向角（量化），无有效重力时固定为「正上方」。 */
@Composable
private fun rememberQuantizedGravityAngle(): State<Float> {
    val tiltState = rememberDeviceTilt()
    return remember(tiltState) {
        derivedStateOf {
            val tilt = tiltState.value
            val magnitudeSquared = tilt.gravityX * tilt.gravityX + tilt.gravityY * tilt.gravityY
            if (magnitudeSquared > GRAVITY_DIR_THRESHOLD_SQ) {
                (atan2(tilt.gravityY, tilt.gravityX) / GRAVITY_ANGLE_STEP_RAD)
                    .roundToInt() * GRAVITY_ANGLE_STEP_RAD
            } else {
                (-PI / 2).toFloat()
            }
        }
    }
}

/**
 * 让 [base] 的主动光源跟着设备的重力方向转，形成「光始终从天上打下来」的错觉。
 *
 * 无传感器的平台（桌面端）[rememberDeviceTilt] 返回 [top.yukonga.miuix.kmp.blur.sensor.DeviceTilt.Zero]，
 * 光源停在正上方，效果稳定不抖。
 *
 * @param extraDegrees 在重力方向之外再转的角度，用来区分底栏（-45°）与选中块（+90°）的高光位置。
 */
@Composable
fun rememberGravityRotatedHighlight(
    base: Highlight,
    extraDegrees: Float = 0f,
): State<Highlight> {
    val baseStyle = base.style as? BloomStroke
    if (baseStyle == null) return remember(base) { derivedStateOf { base } }
    val angle = rememberQuantizedGravityAngle()
    return remember(angle, base, extraDegrees) {
        derivedStateOf {
            val basePrimary = baseStyle.primaryLight
            val rad = angle.value + (extraDegrees * PI / 180.0).toFloat()
            base.copy(
                style = baseStyle.copy(
                    primaryLight = basePrimary.copy(
                        position = LightPosition(
                            x = LIGHT_REF_X + cos(rad),
                            y = LIGHT_REF_Y + sin(rad),
                            z = basePrimary.position.z,
                        ),
                    ),
                ),
            )
        }
    }
}
