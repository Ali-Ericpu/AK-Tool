package org.doctorate.aktool.ui.page.setting

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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


@Preview
@Composable
fun Setting() {
    val context = LocalContext.current
    val config = LocalAppConfig.current.config
    val onConfigChange = LocalAppConfig.current.onConfigChange
    val viewModel: SettingViewModel = viewModel()
    val isUpdateExcel by viewModel.isUpdateExcel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    Column {
        EditText(
            value = config.serverUri,
            label = stringResource(R.string.server_uri),
            onValueSave = {
                onConfigChange(config.copy(serverUri = it))
                Network.createRetrofit(it)
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
        ProgressButton(
            label = "更新资源",
            isUpdate = isUpdateExcel,
            onClick = {
                if (config.serverUri.isEmpty()) {
                    Toast.makeText(context, "链接配置错误", Toast.LENGTH_SHORT).show()
                } else {
                    coroutineScope.launch { viewModel.updateExcel(context, config.serverUri) }
                }
            }
        )
    }
}

@Composable
fun EditText(
    value: String = "",
    label: String = "",
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
            value = value,
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
        Log.d("EditTextDialog", "EditTextDialog")
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
    onValueSave: (String?) -> Unit = { }
) {
    var value by remember { mutableStateOf(value) }
    BasicAlertDialog(
        onDismissRequest = { onValueSave(null) },
        modifier = Modifier
            .width(320.dp)
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .height(80.dp)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            OutlinedTextField(
                value = value,
                maxLines = Int.MAX_VALUE,
                onValueChange = { value = it },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Button(
                    onClick = { onValueSave(null) },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Black.copy(alpha = 0f)
                    )
                ) {
                    Text(
                        text = "取消",
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Button(
                    onClick = { onValueSave(value) },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Black.copy(alpha = 0f)
                    )
                ) {
                    Text(
                        text = "确认",
                        color = MaterialTheme.colorScheme.primary,
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
        Box(Modifier.size(32.dp)) {
            if (isUpdate) {
                CircularProgressIndicator()
            } else {
                IconButton(onClick = { onClick() }) {
                    Icon(Icons.Default.PlayArrow, null)
                }
            }
        }

    }
}