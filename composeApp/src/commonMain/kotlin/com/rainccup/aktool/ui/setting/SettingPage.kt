package com.rainccup.aktool.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

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
            TextButton(label = stringResource(Res.string.choose_bg)) {
                filePicker.pickImage { path -> path?.let { onConfigChange(config.copy(bgPath = it)) } }
            }
            ProgressButton(
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
fun TextButton(
    label: String = "Test",
    onClick: () -> Unit = { },
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.LightGray)
            .padding(8.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label, color = Color.Black, modifier = Modifier
                .padding(8.dp)
                .fillMaxHeight()
        )
        IconButton(
            onClick = { onClick() },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}

@Composable
fun ProgressButton(
    label: String = "Test",
    isUpdate: Boolean = true,
    onClick: () -> Unit = { },
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.LightGray)
            .padding(8.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxHeight()
        )
        Box(
            Modifier
                .padding(end = 4.dp)
                .size(40.dp)
        ) {
            if (isUpdate) {
                CircularProgressIndicator()
            } else {
                IconButton(
                    onClick = { onClick() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                }
            }
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

        Button(
            onClick = { onCancel() },
            colors = ButtonDefaults.buttonColors(
                color = Color.Black.copy(alpha = 0f)
            )
        ) {
            Text(
                text = stringResource(Res.string.cancel),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Button(
            onClick = {
                onConfirm()
            },
            colors = ButtonDefaults.buttonColors(
                color = Color.Black.copy(alpha = 0f),
                disabledColor = Color.Black.copy(alpha = 0f)
            )
        ) {
            Text(
                text = stringResource(Res.string.confirm),
                color = if (error) Color.Red else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}







