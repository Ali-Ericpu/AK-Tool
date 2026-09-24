package com.rainccup.aktool.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomePage(viewModel: HomeViewModel = koinViewModel()) {
    val status by viewModel.status.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = status.nickName,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text("UID: ${status.uid}")
        Text("等级: ${status.level}")
        Text("理智: ${status.ap}/${status.maxAp}")
        Text("龙门币: ${status.gold}")
        Button(
            onClick = { viewModel.refresh() },
            enabled = !isRefreshing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isRefreshing) "刷新中..." else "刷新状态")
        }
    }
}
