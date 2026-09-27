package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun EditText(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String = "",
    hide: Boolean = false,
    singleLine: Boolean = false,
    autoSize: TextAutoSize? = null,
    onValueSave: (String) -> Unit = { },
) {
    var dialogState by remember { mutableStateOf(false) }
    ArrowPreference(
        title = label,
        endActions = {
            Text(
                text = if (hide) "*".repeat(value.length) else value,
                maxLines = 1,
                autoSize = autoSize
            )
        },
        onClick = { dialogState = true },
        holdDownState = dialogState,
    )
    EditTextDialog(
        value = value,
        show = dialogState,
        title = label,
        singleLine = singleLine,
        onConfirm = {
            it?.let { onValueSave(it) }
            dialogState = false
        },
        modifier = modifier
    )
}

@Composable
fun ActionButton(
    label: String = "Test",
    onClick: () -> Unit = { },
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
    ) {
        Text(text = label, style = MiuixTheme.textStyles.button)
    }
}

@Composable
fun ProgressActionButton(
    label: String = "Test",
    isUpdate: Boolean = true,
    onClick: () -> Unit = { },
) {
    Button(
        onClick = onClick,
        enabled = !isUpdate,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
    ) {
        Text(text = label, style = MiuixTheme.textStyles.button)
        if (isUpdate) {
            CircularProgressIndicator(
                modifier = Modifier.padding(start = 8.dp),
                size = 20.dp,
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
