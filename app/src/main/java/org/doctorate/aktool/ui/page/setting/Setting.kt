package org.doctorate.aktool.ui.page.setting

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.doctorate.aktool.R
import org.doctorate.aktool.config.LocalAppConfig
import org.doctorate.aktool.network.Network
import org.doctorate.aktool.ui.page.splash.CircleIconButton
import java.io.File
import java.util.UUID


@Preview
@Composable
fun Setting() {
    val context = LocalContext.current
    val config = LocalAppConfig.current.config
    val onConfigChange = LocalAppConfig.current.onConfigChange
    val viewModel: SettingViewModel = viewModel()
    val isUpdateExcel by viewModel.isUpdateExcel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(uri).use { input ->
                    input?.let {
                        val directory = File(context.filesDir, "data/background/")
                        directory.deleteRecursively()
                        directory.mkdirs()
                        val file = File(directory, "${UUID.randomUUID()}.0")
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                        onConfigChange(config.copy(bgPath = file.absolutePath))
                    }
                }
            }
        }
    )
    LazyColumn(modifier = Modifier.alpha(0.9f)) {
        item {
            EditText(
                value = config.serverUri,
                label = stringResource(R.string.server_uri),
                hide = true,
                onValueSave = {
                    if (Network.initService(it)) {
                        onConfigChange(config.copy(serverUri = it))
                    } else {
                        Toast.makeText(context, R.string.error_uri, Toast.LENGTH_LONG).show()
                    }
                }
            )
            EditText(
                value = config.uid,
                label = stringResource(R.string.uid),
                onValueSave = { onConfigChange(config.copy(uid = it)) }
            )
            EditText(
                value = config.adminKey,
                label = stringResource(R.string.admin_key),
                hide = true,
                onValueSave = { onConfigChange(config.copy(adminKey = it)) }
            )
            EditSwitch(
                label = stringResource(R.string.dark_mode),
                state = config.darkMode,
                onCheckedChange = { onConfigChange(config.copy(darkMode = it)) }
            )
            EditSwitch(
                label = stringResource(R.string.dynamic_color),
                state = config.dynamicColor,
                onCheckedChange = { onConfigChange(config.copy(dynamicColor = it)) }
            )
            EditSwitch(
                label = stringResource(R.string.custom_bg),
                state = config.customBg,
                onCheckedChange = { onConfigChange(config.copy(customBg = it)) }
            )
            TextButton(label = stringResource(R.string.choose_bg)) {
                singleLauncher.launch(arrayOf("image/*"))
            }
            ProgressButton(
                label = stringResource(R.string.update_excel),
                isUpdate = isUpdateExcel,
                onClick = {
                    if (config.serverUri.isEmpty()) {
                        Toast.makeText(context, R.string.error_uri, Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch { viewModel.updateExcel(context, config.serverUri) }
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
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.LightGray)
            .padding(8.dp)
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        OutlinedTextField(
            singleLine = true,
            readOnly = true,
            value = if (hide) "*".repeat(value.length) else value,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            onValueChange = { },
            label = { Text(text = label, color = Color.Black) },
            modifier = Modifier
                .weight(8f)
                .padding(bottom = 4.dp)
        )
        Box(modifier = Modifier.weight(2f)) {
            CircleIconButton(
                icon = Icons.Default.Edit,
                onClick = { dialogState = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextDialog(
    value: String = "",
    label: String = "Test",
    error: (String) -> Boolean = { false },
    onValueSave: (String?) -> Unit = { }
) {
    val context = LocalContext.current
    var value by remember { mutableStateOf(value) }
    val error = error(value)
    BasicAlertDialog(
        onDismissRequest = { onValueSave(null) },
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                value = value,
                maxLines = Int.MAX_VALUE,
                isError = error,
                onValueChange = { value = it },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Button(
                    onClick = { onValueSave(null) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Button(
                    onClick = {
                        if (error) {
                            Toast.makeText(context, R.string.error_data, Toast.LENGTH_SHORT).show()
                        } else {
                            onValueSave(value)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0f),
                        disabledContainerColor = Color.Black.copy(alpha = 0f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        color = if (error) Color.Red else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun EditSwitch(
    label: String,
    state: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    var state by remember { mutableStateOf(state) }
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
        Switch(
            checked = state,
            onCheckedChange = { onCheckedChange(it.also { state = it }) }
        )
    }
}

@Preview
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

@Preview
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