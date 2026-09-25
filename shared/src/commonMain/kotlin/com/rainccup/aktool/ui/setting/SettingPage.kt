package com.rainccup.aktool.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rainccup.aktool.config.LocalAppConfig
import com.rainccup.aktool.core.network.HttpClientProvider
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.admin_key
import com.rainccup.aktool.resources.cancel
import com.rainccup.aktool.resources.choose_bg
import com.rainccup.aktool.resources.confirm
import com.rainccup.aktool.resources.custom_bg
import com.rainccup.aktool.resources.dark_mode
import com.rainccup.aktool.resources.server_uri
import com.rainccup.aktool.resources.uid
import com.rainccup.aktool.resources.update_excel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingPage() {
    val config = LocalAppConfig.current.config
    val onConfigChange = LocalAppConfig.current.onConfigChange
    val viewModel: SettingViewModel = koinViewModel()
    val isUpdateExcel by viewModel.isUpdateExcel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val filePicker: com.rainccup.aktool.core.platform.FilePicker = koinInject()
    val httpClientProvider: HttpClientProvider = koinInject()
    LazyColumn(modifier = Modifier.alpha(0.9f)) {
        item {
            EditText(
                value = config.serverUri,
                label = stringResource(Res.string.server_uri),
                hide = true,
                onValueSave = {
                    if (it.isNotBlank()) {
                        httpClientProvider.recreate(it)
                        onConfigChange(config.copy(serverUri = it))
                    }
                }
            )
            EditText(
                value = config.uid,
                label = stringResource(Res.string.uid),
                onValueSave = { onConfigChange(config.copy(uid = it)) }
            )
            EditText(
                value = config.adminKey,
                label = stringResource(Res.string.admin_key),
                hide = true,
                onValueSave = { onConfigChange(config.copy(adminKey = it)) }
            )
            EditSwitch(
                label = stringResource(Res.string.dark_mode),
                state = config.darkMode,
                onCheckedChange = { onConfigChange(config.copy(darkMode = it)) }
            )
            EditSwitch(
                label = stringResource(Res.string.custom_bg),
                state = config.customBg,
                onCheckedChange = { onConfigChange(config.copy(customBg = it)) }
            )
            ActionButton(label = stringResource(Res.string.choose_bg)) {
                filePicker.pickImage { path -> path?.let { onConfigChange(config.copy(bgPath = it)) } }
            }
            ProgressActionButton(
                label = stringResource(Res.string.update_excel),
                isUpdate = isUpdateExcel,
                onClick = {
                    if (config.serverUri.isEmpty()) {
                        
                    } else {
                        coroutineScope.launch { viewModel.updateExcel(config.serverUri) }
                    }
                }
            )
        }
    }
}

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







