package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BasicDialog(
    label: String = "Test",
    error: Boolean = false,
    onCancel: () -> Unit = { },
    onConfirm: () -> Unit = { },
    content: @Composable (ColumnScope.() -> Unit) = {}
) {
    OverlayDialog(
        title = label,
        show = true,
        onDismissRequest = { onCancel() },
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
            ConfirmButtonRow(
                error = error,
                onCancel = { onCancel() },
                onConfirm = { onConfirm() },
            )
        }
    }
}

@Composable
fun EditTextDialog(
    value: String = "",
    label: String = "Test",
    error: (String) -> Boolean = { false },
    onValueSave: (String?) -> Unit = { }
) {
    var text by remember { mutableStateOf(value) }
    val isError = error(text)
    BasicDialog(
        label = label,
        error = isError,
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (isError) {

            } else {
                onValueSave(text)
            }
        }
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            maxLines = Int.MAX_VALUE,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ConfirmButtonRow(
    error: Boolean = false,
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
        TextButton(
            text = stringResource(Res.string.cancel),
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(
                textColor = MiuixTheme.colorScheme.onBackground,
            ),
        )
        TextButton(
            text = stringResource(Res.string.confirm),
            onClick = onConfirm,
            colors = ButtonDefaults.textButtonColors(
                textColor = if (error) Color.Red else MiuixTheme.colorScheme.primary,
            ),
        )
    }
}
