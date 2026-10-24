// Adapted from compose-miuix-ui's IosLiquidGlassNavigationBar example (Apache-2.0),
// via KernelSU's FloatingBottomBar port. The upstream version paints the fingertip
// glow with an AGSL RuntimeShader; a radial gradient gives the same look on every
// platform (AGSL only exists on Android 13+ and would silently vanish on desktop).

package com.rainccup.aktool.core.designsystem.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 手指按在玻璃上时，触点周围亮一圈 —— 液态玻璃的「交互高光」。
 *
 * @param animationScope 驱动动画的协程作用域。
 * @param position 把触点换算成高光中心（例如跟随选中块而不是手指）。
 */
class InteractiveHighlight(
    val animationScope: CoroutineScope,
    val position: (size: Size, offset: Offset) -> Offset = { _, offset -> offset },
) {

    private val pressProgressAnimationSpec = spring(0.5f, 300f, 0.001f)
    private val positionAnimationSpec = spring(0.5f, 300f, Offset.VisibilityThreshold)

    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val positionAnimation =
        Animatable(Offset.Zero, Offset.VectorConverter, Offset.VisibilityThreshold)

    private var startPosition = Offset.Zero

    val modifier: Modifier = Modifier.drawWithContent {
        val progress = pressProgressAnimation.value
        if (progress > 0f) {
            // 整片微微提亮，模拟手指挡住环境光后玻璃更「透」。
            drawRect(
                Color.White.copy(alpha = 0.06f * progress),
                blendMode = BlendMode.Plus,
            )
            // 触点周围的柔光：中心 12% 白，到 radius 处完全透明。
            val center = position(size, positionAnimation.value)
            val radius = size.minDimension * 1.2f
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color.White.copy(alpha = 0.12f * progress),
                    0.5f to Color.White.copy(alpha = 0.03f * progress),
                    1f to Color.Transparent,
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
                blendMode = BlendMode.Plus,
            )
        }

        drawContent()
    }

    /** 按下：高光中心对齐触点并开始发亮。 */
    fun press(offset: Offset) {
        startPosition = offset
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            launch { positionAnimation.snapTo(offset) }
        }
    }

    /** 拖动中：高光中心跟着手指。 */
    fun move(offset: Offset) {
        animationScope.launch { positionAnimation.snapTo(offset) }
    }

    /** 松手：淡出并回位。 */
    fun release() {
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { positionAnimation.animateTo(startPosition, positionAnimationSpec) }
        }
    }
}
