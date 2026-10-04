package com.rainccup.aktool.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
fun EditText(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String = "",
    hide: Boolean = false,
    singleLine: Boolean = false,
    onValueSave: (String) -> Unit = { },
) {
    var dialogState by remember { mutableStateOf(false) }
    ArrowPreference(
        title = label,
        endActions = {
            Text(
                text = if (hide) "*".repeat(value.length) else value,
                maxLines = 1,
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
