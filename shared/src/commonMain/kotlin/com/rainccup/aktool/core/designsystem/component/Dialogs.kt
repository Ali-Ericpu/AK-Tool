package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.cancel
import com.rainccup.aktool.resources.confirm
import com.rainccup.aktool.resources.content_invalid
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BasicDialog(
    title: String,
    show: Boolean,
    summary: String? = null,
    summaryColor: Color = DialogDefaults.summaryColor(),
    error: Boolean = false,
    neutralText: String? = null,
    onNeutral: () -> Unit = { },
    onCancel: () -> Unit = { },
    onConfirm: () -> Unit = { },
    content: @Composable (ColumnScope.() -> Unit) = {}
) {
    OverlayDialog(
        title = title,
        summary = summary,
        summaryColor = summaryColor,
        show = show,
        onDismissRequest = onCancel,
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
            ConfirmButtonRow(
                error = error,
                neutralText = neutralText,
                onNeutral = onNeutral,
                onCancel = onCancel,
                onConfirm = onConfirm,
            )
        }
    }
}

@Composable
fun EditTextDialog(
    show: Boolean,
    modifier: Modifier = Modifier,
    value: String = "",
    title: String = "Title",
    error: (String) -> Boolean = { false },
    singleLine: Boolean = false,
    onConfirm: (String?) -> Unit = { },
    onCancel: () -> Unit = { onConfirm(null) }
) {
    var text by remember(show) { mutableStateOf(value) }
    val isError = error(text)
    BasicDialog(
        title = title,
        show = show,
        // 校验不通过时给出可见原因，而不是让「确定」静默失效
        summary = if (isError) stringResource(Res.string.content_invalid) else null,
        summaryColor = MaterialTheme.colorScheme.error,
        error = isError,
        onCancel = onCancel,
        onConfirm = {
            // 非法输入不提交、也不关闭弹窗：红色提示让用户继续修改。
            if (!isError) {
                onConfirm(text)
            }
        }
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            singleLine = singleLine,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ConfirmButtonRow(
    error: Boolean = false,
    neutralText: String? = null,
    onNeutral: () -> Unit = { },
    onCancel: () -> Unit = { },
    onConfirm: () -> Unit = { }
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Right,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        if (neutralText != null) {
            TextButton(
                text = neutralText,
                onClick = onNeutral,
                colors = ButtonDefaults.textButtonColors(
                    color = Color.Transparent,
                    textColor = MiuixTheme.colorScheme.onBackground,
                ),
            )
        }
        TextButton(
            text = stringResource(Res.string.cancel),
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(
                color = Color.Transparent,
                textColor = MiuixTheme.colorScheme.onBackground,
            ),
        )
        TextButton(
            text = stringResource(Res.string.confirm),
            onClick = onConfirm,
            colors = ButtonDefaults.textButtonColors(
                color = Color.Transparent,
                textColor = if (error) Color.Red else MiuixTheme.colorScheme.primary,
            ),
        )
    }
}
