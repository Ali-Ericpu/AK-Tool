package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.cancel
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 低于这个宽度按手机排版（按钮等分铺满），达到或超过则按桌面排版（收缩靠右）。 */
private val CompactWidthLimit = 480.dp

/**
 * 页面级动作行：一套样式同时照顾移动端与桌面端。
 *
 * - **窄屏（手机 / 窄窗）**：两个按钮等分整行宽度——热区大、拇指容易够到；
 * - **宽屏（桌面）**：按钮收缩为内容宽度并整体靠右——符合桌面「动作在尾部」的惯例，
 *   也避免两个撑满整屏的大色块；
 * - **主次分明**：主操作填色（高强调），次要操作透明文字（低强调）。这是 Material 与
 *   桌面系统共同的层级约定，所以两端都不别扭；
 * - **高度**沿用 Miuix 默认的 40dp（`ButtonDefaults.MinHeight`）：手机上够大，桌面上不松散。
 *
 * 这里是页面级动作，宽度可能从手机跨度到桌面，需要按宽度切换排版。
 */
@Composable
fun PageActionRow(
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String? = null,
    onCancel: () -> Unit = { },
    confirmEnabled: Boolean = true,
) {
    val resolvedCancelText = cancelText ?: stringResource(Res.string.cancel)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val useCompactLayout = maxWidth < CompactWidthLimit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (useCompactLayout) {
                Arrangement.spacedBy(12.dp)
            } else {
                Arrangement.spacedBy(8.dp, Alignment.End)
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (useCompactLayout) {
                SecondaryActionButton(resolvedCancelText, onCancel, Modifier.weight(1f))
                PrimaryActionButton(confirmText, onConfirm, Modifier.weight(1f), confirmEnabled)
            } else {
                SecondaryActionButton(resolvedCancelText, onCancel, Modifier)
                PrimaryActionButton(confirmText, onConfirm, Modifier, confirmEnabled)
            }
        }
    }
}

@Composable
private fun SecondaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier) {
    TextButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            color = Color.Transparent,
            textColor = MiuixTheme.colorScheme.onBackground,
        ),
    )
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(
            text = text,
            color = MiuixTheme.colorScheme.primary
        )
    }
}
