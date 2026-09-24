package com.rainccup.aktool.ui.extra

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExtraPage(viewModel: ExtraViewModel = koinViewModel()) {
    val isConnecting by viewModel.isConnecting.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("更多操作", style = MaterialTheme.typography.headlineSmall)
        Button(
            onClick = { viewModel.unlockAllStages() },
            enabled = !isConnecting,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("解锁所有关卡") }
        Button(
            onClick = { viewModel.unlockAllFlags() },
            enabled = !isConnecting,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("解锁所有检查点") }
        OutlinedButton(
            onClick = { viewModel.resetRlv2() },
            enabled = !isConnecting,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("重置集成战略状态") }
        Button(
            onClick = { viewModel.syncValidCode() },
            enabled = !isConnecting,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("查询可用验证码") }
        OutlinedButton(
            onClick = { viewModel.queryAccountByUID() },
            enabled = !isConnecting,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("查询账号") }
        viewModel.validateCodeList().forEach { (code, _) ->
            Text(code, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
