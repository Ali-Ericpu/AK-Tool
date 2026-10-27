package com.rainccup.aktool.feature.characterdetail.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rainccup.aktool.core.designsystem.component.BasicDialog
import com.rainccup.aktool.core.designsystem.component.roundedBackground
import com.rainccup.aktool.feature.character.ui.portraitPainter
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.skin
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.preference.ArrowPreference

/** 缩略图宽高比（宽 / 高）。想调竖图长短只改这里：2/3.8 ≈ 竖版，3/4 ≈ 略方。 */
private const val SkinTileAspect = 2f / 3.8f

/**
 * 服装切换：入口是一行 [ArrowPreference]，点开后是缩略图网格。
 *
 * 为什么用网格而不是列表 + 复选框：服装是**视觉**选择，缩略图本身就该是主要信息。
 * 旧实现给每张立绘一个 120dp 宽、没有高度约束的 `Image(Crop)`，行高按原图比例随机撑开，
 * 裁切位置也不可控；现在每格固定竖版比例（[SkinTileAspect]）、圆角裁切，选中靠
 * **描边 + 右上角对勾**表达，名称放在缩略图下方，长度超出时两行截断。
 */
@Composable
fun ChangeSkinDialog(
    current: String,
    skinList: List<Pair<String, String>>,
    onConfirm: (String) -> Unit
) {
    var show by remember { mutableStateOf(false) }
    var skin by remember(show) { mutableStateOf(current) }

    ArrowPreference(
        title = stringResource(Res.string.skin),
        summary = skinList.firstOrNull { it.first == current }?.second,
        onClick = { show = true },
        modifier = Modifier
            .padding(4.dp)
            .roundedBackground()
    )
    BasicDialog(
        title = stringResource(Res.string.skin),
        show = show,
        // 网格自己就是滚动容器（懒加载），交给 BasicDialog 再包一层滚动会以内层无限高约束测量而崩
        contentScrollable = false,
        onCancel = { show = false },
        onConfirm = {
            onConfirm(skin)
            show = false
        }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
        ) {
            items(skinList, key = { it.first }) { (skinId, skinName) ->
                SkinTile(
                    selected = skinId == skin,
                    name = skinName,
                    painter = portraitPainter(skinId),
                    onClick = { skin = skinId },
                )
            }
        }
    }
}

/** 单个服装缩略图：竖版圆角裁切（[SkinTileAspect]），选中时描边并在右上角打勾。 */
@Composable
private fun SkinTile(
    selected: Boolean,
    name: String,
    painter: Painter,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(10.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(SkinTileAspect)
                .clip(shape)
                // 立绘来自网络，加载完成前也有一块有意的底色，不会出现镂空
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (selected) Modifier.border(2.dp, accent, shape) else Modifier
                )
        ) {
            Image(
                painter = painter,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (selected) {
                Checkbox(
                    state = ToggleableState.On,
                    // 只当选中标记：点选由整格处理，避免两个点击区。
                    onClick = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                )
            }
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, start = 2.dp, end = 2.dp),
        )
    }
}
