package com.rainccup.aktool.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rainccup.aktool.core.datastore.ConfigRepository
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingPage(viewModel: SettingViewModel = koinViewModel()) {
    val configRepository: ConfigRepository = koinInject()
    val isUpdate by viewModel.isUpdateExcel.collectAsState()
    var config by remember { mutableStateOf(configRepository.read()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("设置", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = config.serverUri,
            onValueChange = { config = config.copy(serverUri = it) },
            label = { Text("服务器链接") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = config.uid,
            onValueChange = { config = config.copy(uid = it) },
            label = { Text("UID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = config.adminKey,
            onValueChange = { config = config.copy(adminKey = it) },
            label = { Text("Admin Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        RowSwitch(
            label = "暗黑模式",
            checked = config.darkMode,
            onCheckedChange = {
                config = config.copy(darkMode = it)
                configRepository.write(config)
            },
        )
        RowSwitch(
            label = "动态取色",
            checked = config.dynamicColor,
            onCheckedChange = {
                config = config.copy(dynamicColor = it)
                configRepository.write(config)
            },
        )
        RowSwitch(
            label = "使用自定义背景",
            checked = config.customBg,
            onCheckedChange = {
                config = config.copy(customBg = it)
                configRepository.write(config)
            },
        )
        Button(
            onClick = {
                viewModel.saveConfig(config.serverUri, config.uid, config.adminKey)
                config = configRepository.read()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("保存") }

        OutlinedButton(
            onClick = { viewModel.updateExcel(config.serverUri) },
            enabled = !isUpdate && config.serverUri.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (isUpdate) "更新中..." else "更新资源") }
    }
}

@Composable
private fun RowSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
