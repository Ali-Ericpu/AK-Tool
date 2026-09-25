package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun EditText(
    value: String = "",
    label: String = "",
    hide: Boolean = false,
    onValueSave: (String) -> Unit = { }
) {
    var dialogState by remember { mutableStateOf(false) }
    ArrowPreference(
        title = label,
        endActions = {
            Text(text = if (hide) "*".repeat(value.length) else value)
        },
        onClick = { dialogState = true },
        holdDownState = dialogState,
    )

    if (dialogState) {
        EditTextDialog(
            value = value,
            label = label,
            onValueSave = {
                dialogState = false
                it?.let { onValueSave(it) }
            }
        )
    }
}

@Composable
fun EditSwitch(
    label: String,
    state: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SwitchPreference(
        title = label,
        checked = state,
        onCheckedChange = { onCheckedChange(it) },
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
