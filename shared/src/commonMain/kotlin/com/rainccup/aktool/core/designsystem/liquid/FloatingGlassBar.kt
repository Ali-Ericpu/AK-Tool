// Adapted from compose-miuix-ui's IosLiquidGlassNavigationBar example (Apache-2.0),
// via KernelSU's FloatingBottomBar port.

package com.rainccup.aktool.core.designsystem.liquid

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.onClick as semanticsOnClick
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.highlight.LightSource
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight

/** 按压时内容的放大倍数载体，选中块会把它的内容放大到 1.2 倍。 */
private val LocalFloatingBarTabScale = staticCompositionLocalOf { { 1f } }

/**
 * 底栏的一项：图标在上、文字在下。
 *
 * 颜色不在这里判断选中与否 —— 底层用未选中的颜色画一遍，另有一层「全 primary」的副本
 * 被选中块裁出来显示（见 [FloatingGlassBar]）。这样选中染色只发生在块内，
 * 而且不用为每一项跑颜色动画。
 */
@Composable
fun RowScope.GlassBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val scale = LocalFloatingBarTabScale.current
    Column(
        modifier
            // 点击与拖动统一由底栏的唯一手势识别器分发；子节点挂 selectable 会先吃掉按下事件。
            // 可访问性用无障碍动作补回（TalkBack 仍能激活）。
            .semantics {
                role = Role.Tab
                this.selected = selected
                semanticsOnClick(label = label) {
                    onClick()
                    true
                }
            }
            .onKeyEvent { event ->
                // 桌面端 / 外接键盘：回车与空格等同于点击。
                val activationKey = event.key == Key.Enter ||
                    event.key == Key.NumPadEnter || event.key == Key.Spacebar
                if (activationKey) {
                    if (event.type == KeyEventType.KeyUp) onClick()
                    true
                } else {
                    false
                }
            }
            .focusable()
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                val s = scale()
                scaleX = s
                scaleY = s
            },
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            // 下面的文字已经把这个项念出来了，图标再带一次描述会读两遍。
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            color = contentColor,
            style = MiuixTheme.textStyles.footnote2,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** 底栏的一个标签页。[icon] 由调用方给，底栏只负责排版与选中态。 */
@Immutable
class GlassBarTab(
    val label: String,
    val icon: ImageVector,
)

// 选中块的边缘高光：中心偏上、下方补一盏弱光，dualPeak 让边缘出现两个对称的亮斑。
private val PillSpecular: Highlight = Highlight(
    width = 1.dp,
    alpha = 1f,
    style = BloomStroke(
        color = Color.White.copy(alpha = 0.12f),
        innerBlurRadius = 2.0.dp,
        primaryLight = LightSource(
            position = LightPosition(0.5f, -0.3f, -0.05f),
            color = Color.White,
            intensity = 1f,
        ),
        secondaryLight = LightSource(
            position = LightPosition(0.5f, 0.8f, -0.5f),
            color = Color.White,
            intensity = 0.4f,
        ),
        dualPeak = true,
    ),
)

/**
 * 液态玻璃悬浮底栏。
 *
 * 由三层叠出来（自下而上）：
 * 1. 玻璃本体：`drawBackdrop` 采样 [backdrop]，加饱和、模糊、边缘折射，再画一层半透明底色；
 * 2. 选中态染色层：一份 `alpha = 0` 的副本，先用 `layerBackdrop` 录成图层，再用 primary 上色；
 * 3. 选中块：一块会滑动的胶囊，它的 backdrop 是「页面 + 染色层」的合并体，
 *    于是块内透出的图标是 primary，块外仍是未选中的颜色。
 *
 * 整个底栏可拖动：手指按住横向拖动会在各项之间连续滑动，松手弹到最近的项。
 *
 * @param selectedIndex 当前选中项。
 * @param onSelected 选中项变化（点击或拖动结束）。
 * @param tabs 各标签页的图标与文字。
 * @param backdrop 玻璃采样的背景图层（应包含页面内容与背景图）。
 * @param isBlurEnabled 关掉后退化为不透明底栏（低性能设备 / 测试用）。
 */
@Composable
fun FloatingGlassBar(
    selectedIndex: Int,
    onSelected: (index: Int) -> Unit,
    tabs: List<GlassBarTab>,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isBlurEnabled: Boolean = true,
) {
    if (tabs.isEmpty()) return
    val tabsCount = tabs.size
    val isInDark = MiuixTheme.colorScheme.surface.luminance() < 0.5f
    val pillShape = remember { CircleShape }
    val accentColor = MiuixTheme.colorScheme.primary
    val tabContentColor = MiuixTheme.colorScheme.onSurfaceContainer.copy(alpha = 0.72f)
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val containerColor = if (isBlurEnabled) surfaceContainer.copy(0.4f) else surfaceContainer

    // 染色层自己的 backdrop：把「primary 色的那一层」录进来，供选中块采样。
    val tabsBackdrop = rememberLayerBackdrop()
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()

    var tabWidthPx by remember { mutableFloatStateOf(0f) }
    var totalWidthPx by remember { mutableFloatStateOf(0f) }

    // 拖过头时栏体被「橡皮筋」拉住的一点点位移。
    val offsetAnimation = remember { Animatable(0f) }
    val rubberBandPx = with(density) { 4.dp.toPx() }
    val panelOffset by remember(rubberBandPx) {
        derivedStateOf {
            if (totalWidthPx == 0f) {
                0f
            } else {
                val fraction = (offsetAnimation.value / totalWidthPx).fastCoerceIn(-1f, 1f)
                rubberBandPx * kotlin.math.sign(fraction) * EaseOut.transform(kotlin.math.abs(fraction))
            }
        }
    }

    var currentIndex by remember { mutableIntStateOf(selectedIndex) }
    val onSelectedUpdated by rememberUpdatedState(onSelected)

    fun indexAt(positionX: Float): Int {
        if (tabWidthPx == 0f) return currentIndex
        val horizontalPaddingPx = with(density) { 4.dp.toPx() }
        val logicalX = if (isLtr) positionX else totalWidthPx - positionX
        return ((logicalX - horizontalPaddingPx) / tabWidthPx)
            .toInt()
            .coerceIn(0, tabsCount - 1)
    }

    val dampedDragAnimation = remember(animationScope, tabsCount, density, isLtr) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = selectedIndex.toFloat(),
            valueRange = 0f..(tabsCount - 1).toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 78f / 56f,
            canDrag = { offset -> offset.x in 0f..totalWidthPx },
            onDragStarted = { position ->
                updateValue(indexAt(position.x).toFloat())
            },
            onDragStopped = {
                val targetIndex = targetValue.roundToInt().coerceIn(0, tabsCount - 1)
                if (currentIndex != targetIndex) {
                    currentIndex = targetIndex
                    onSelectedUpdated(targetIndex)
                }
                updateValue(targetIndex.toFloat())
                animationScope.launch {
                    offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                }
            },
            onDragCancelled = {
                updateValue(currentIndex.toFloat())
                animationScope.launch {
                    offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                }
            },
            onDrag = { dragAmount ->
                if (tabWidthPx > 0f && dragAmount.x != 0f) {
                    updateValue(
                        (targetValue + dragAmount.x / tabWidthPx * if (isLtr) 1f else -1f)
                            .coerceIn(0f, (tabsCount - 1).toFloat()),
                    )
                    animationScope.launch {
                        offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x)
                    }
                }
            },
        )
    }

    // 外部（路由变化）改选中项时，把选中块弹过去。
    LaunchedEffect(selectedIndex) {
        if (currentIndex != selectedIndex) {
            currentIndex = selectedIndex
            dampedDragAnimation.animateToValue(selectedIndex.toFloat())
        }
    }

    fun activateTab(index: Int) {
        if (index !in 0 until tabsCount) return
        if (currentIndex != index) {
            currentIndex = index
            onSelectedUpdated(index)
        }
        dampedDragAnimation.animateToValue(index.toFloat())
    }

    val interactiveHighlight = remember(animationScope, tabWidthPx, dampedDragAnimation) {
        InteractiveHighlight(
            animationScope = animationScope,
            position = { size, _ ->
                Offset(
                    if (isLtr) {
                        (dampedDragAnimation.value + 0.5f) * tabWidthPx + panelOffset
                    } else {
                        size.width - (dampedDragAnimation.value + 0.5f) * tabWidthPx + panelOffset
                    },
                    size.height / 2f,
                )
            },
        )
    }

    val baseHighlight = rememberGravityRotatedHighlight(PillSpecular, extraDegrees = -45f)
    val pillHighlight = rememberGravityRotatedHighlight(PillSpecular, extraDegrees = 90f)

    val combinedBackdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop)

    Box(
        // 撑满父容器：项用 weight 平分宽度，pill 的宽度才能算成 (总宽 - 8dp) / 项数。
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        // ---- 第 1 层：玻璃本体 + 未着色内容 ----
        Row(
            Modifier
                .onGloballyPositioned { coords ->
                    totalWidthPx = coords.size.width.toFloat()
                    val contentWidthPx = totalWidthPx - with(density) { 8.dp.toPx() }
                    tabWidthPx = (contentWidthPx / tabsCount).coerceAtLeast(0f)
                }
                .selectableGroup()
                .graphicsLayer { translationX = panelOffset }
                .dropShadow(
                    shape = pillShape,
                    shadow = Shadow(
                        radius = 10.dp,
                        color = Color.Black,
                        alpha = if (isInDark) 0.2f else 0.1f,
                    ),
                )
                .then(
                    if (isBlurEnabled) {
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { pillShape },
                            effects = {
                                // 折射要靠形状外的像素，先把采样层的 padding 撑开。
                                padding = maxOf(padding, 40.dp.toPx())
                                vibrancy()
                                blur(4.dp.toPx(), 4.dp.toPx())
                                lens(
                                    refractionHeight = 24.dp.toPx(),
                                    refractionAmount = 24.dp.toPx(),
                                )
                            },
                            highlight = { baseHighlight.value.copy(alpha = 0.75f) },
                            layerBlock = {
                                // 按压时整体横向「鼓」一点点。
                                val width = size.width.coerceAtLeast(1f)
                                val s = lerp(
                                    1f,
                                    1f + 16.dp.toPx() / width,
                                    dampedDragAnimation.pressProgress,
                                )
                                scaleX = s
                                scaleY = s
                            },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                    } else {
                        Modifier.background(containerColor, pillShape)
                    },
                )
                .then(if (isBlurEnabled) interactiveHighlight.modifier else Modifier)
                .pointerInput(tabsCount, tabWidthPx, isLtr, isBlurEnabled) {
                    // 全栏唯一的手势识别器：点击与拖动都从这里分发。之前高光与选中块各自装了
                    // detectDragGestures，先启动的那个越过 touch slop 后会 consume 事件，
                    // 另一个就永远等不到拖动 —— 表现就是「只能点，不能滑」。
                    val touchSlop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        dampedDragAnimation.press()
                        if (isBlurEnabled) interactiveHighlight.press(down.position)
                        var dragging = false
                        var travelled = 0f
                        var releaseX = down.position.x
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            releaseX = change.position.x
                            if (!change.pressed) {
                                change.consume()
                                break
                            }
                            val dx = change.positionChange().x
                            travelled += dx
                            if (!dragging && abs(travelled) > touchSlop) {
                                dragging = true
                                dampedDragAnimation.updateValue(indexAt(change.position.x).toFloat())
                            }
                            if (dragging && tabWidthPx > 0f) {
                                dampedDragAnimation.updateValue(
                                    dampedDragAnimation.targetValue +
                                        dx / tabWidthPx * if (isLtr) 1f else -1f,
                                )
                                animationScope.launch {
                                    offsetAnimation.snapTo(offsetAnimation.value + dx)
                                }
                                if (isBlurEnabled) interactiveHighlight.move(change.position)
                            }
                            change.consume()
                        }
                        if (isBlurEnabled) interactiveHighlight.release()
                        if (dragging) {
                            val target = dampedDragAnimation.value
                                .roundToInt()
                                .coerceIn(0, tabsCount - 1)
                            dampedDragAnimation.animateToValue(target.toFloat())
                            if (currentIndex != target) {
                                currentIndex = target
                                onSelectedUpdated(target)
                            }
                            animationScope.launch {
                                offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f))
                            }
                        } else {
                            dampedDragAnimation.release()
                            activateTab(indexAt(releaseX))
                        }
                    }
                }
                .height(64.dp)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                GlassBarItem(
                    selected = index == currentIndex,
                    onClick = { activateTab(index) },
                    icon = tab.icon,
                    label = tab.label,
                    contentColor = tabContentColor,
                )
            }
        }

        // ---- 第 2 层：全 primary 的副本，录进 tabsBackdrop，本身不显示 ----
        if (isBlurEnabled) {
            CompositionLocalProvider(
                LocalFloatingBarTabScale provides {
                    lerp(1f, 1.2f, dampedDragAnimation.pressProgress)
                },
                LocalContentColor provides accentColor,
            ) {
                Row(
                    Modifier
                        .clearAndSetSemantics {}
                        .alpha(0f)
                        .layerBackdrop(tabsBackdrop)
                        .graphicsLayer { translationX = panelOffset }
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { pillShape },
                            effects = {
                                vibrancy()
                                blur(4.dp.toPx(), 4.dp.toPx())
                                lens(
                                    refractionHeight = 24.dp.toPx(),
                                    refractionAmount = 24.dp.toPx(),
                                )
                            },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                        .then(interactiveHighlight.modifier)
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        GlassBarItem(
                            selected = index == currentIndex,
                            onClick = { activateTab(index) },
                            icon = tab.icon,
                            label = tab.label,
                            contentColor = accentColor,
                        )
                    }
                }
            }
        }

        // ---- 第 3 层：会滑动的选中块 ----
        if (tabWidthPx > 0f) {
            val tabWidthDp = with(density) { tabWidthPx.toDp() }
            if (isBlurEnabled) {
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            val progressOffset = dampedDragAnimation.value * tabWidthPx
                            translationX = if (isLtr) {
                                progressOffset + panelOffset
                            } else {
                                -progressOffset + panelOffset
                            }
                        }
                        .drawBackdrop(
                            backdrop = combinedBackdrop,
                            shape = { pillShape },
                            effects = {
                                // 只有按压时才出现折射与色散，按下瞬间像玻璃被压弯。
                                val progress = dampedDragAnimation.pressProgress
                                lens(
                                    refractionHeight = 10.dp.toPx() * progress,
                                    refractionAmount = 14.dp.toPx() * progress,
                                    depthEffect = true,
                                    chromaticAberration = 0.5f,
                                )
                            },
                            highlight = {
                                pillHighlight.value.copy(alpha = dampedDragAnimation.pressProgress)
                            },
                            layerBlock = {
                                scaleX = dampedDragAnimation.scaleX
                                scaleY = dampedDragAnimation.scaleY
                                // 甩动时沿速度方向拉长，松手回正 —— 惯性感。
                                val velocity = dampedDragAnimation.velocity / 10f
                                scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                                scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                            },
                            onDrawSurface = {
                                val progress = dampedDragAnimation.pressProgress
                                drawRect(
                                    color = if (!isInDark) {
                                        Color.Black.copy(alpha = 0.1f)
                                    } else {
                                        Color.White.copy(alpha = 0.1f)
                                    },
                                    alpha = 1f - progress,
                                )
                                drawRect(Color.Black.copy(alpha = 0.03f * progress))
                            },
                        )
                        .innerShadow(shape = pillShape) {
                            InnerShadow(
                                radius = 8.dp * dampedDragAnimation.pressProgress,
                                color = Color.Black.copy(alpha = 0.15f),
                                alpha = dampedDragAnimation.pressProgress,
                            )
                        }
                        .height(56.dp)
                        .width(tabWidthDp),
                )
            } else {
                // 无模糊时用纯色块近似，内容用裁剪出来的 primary 副本。
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .graphicsLayer {
                            val progressOffset = dampedDragAnimation.value * tabWidthPx
                            translationX = if (isLtr) {
                                progressOffset + panelOffset
                            } else {
                                -progressOffset + panelOffset
                            }
                        }
                        .clip(pillShape)
                        .background(accentColor.copy(alpha = 0.15f), pillShape)
                        .height(56.dp)
                        .width(tabWidthDp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    CompositionLocalProvider(LocalContentColor provides accentColor) {
                        Row(
                            Modifier
                                .clearAndSetSemantics {}
                                .wrapContentWidth(align = Alignment.Start, unbounded = true)
                                .requiredWidth(with(density) { (totalWidthPx - 8.dp.toPx()).toDp() })
                                .height(56.dp)
                                .graphicsLayer {
                                    val progressOffset = dampedDragAnimation.value * tabWidthPx
                                    translationX = if (isLtr) -progressOffset else progressOffset
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                GlassBarItem(
                                    selected = index == currentIndex,
                                    onClick = { activateTab(index) },
                                    icon = tab.icon,
                                    label = tab.label,
                                    contentColor = accentColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
