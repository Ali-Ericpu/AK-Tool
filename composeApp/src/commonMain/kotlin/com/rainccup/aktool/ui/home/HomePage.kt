package com.rainccup.aktool.ui.home


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.*
import com.rainccup.aktool.core.model.Status
import com.rainccup.aktool.core.model.SaveStatusRequest
import com.rainccup.aktool.ui.setting.EditTextDialog

@Composable
fun HomePage() {
    val viewModel: HomeViewModel = koinViewModel()
    val isSplash by viewModel.isSplash.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val status: Status by viewModel.status.collectAsState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (isSplash) {
            if (!isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier
                            .padding(4.dp)
                            .size(72.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.baseline_home_start),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(16.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    LabelTextField(
                        value = status.nickName,
                        label = stringResource(Res.string.nick_name),
                        error = { text ->
                            text.toCharArray().sumOf { if (it.code > 255) 2L else 1L } > 16
                        },
                        onValueSave = {
                            viewModel.updateStatus(SaveStatusRequest(nickName = it))
                        },
                    )
                    Row {
                        LabelTextField(
                            value = status.level.toString(),
                            label = stringResource(Res.string.level),
                            error = {
                                val int = it.toIntOrNull()
                                int == null || int !in 1..120
                            },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(level = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.nickNumber,
                            label = stringResource(Res.string.nick_num),
                            error = { text ->
                                text.length != 4 || text.any { it.code !in 48..57 }
                            },
                            onValueSave = {
                                viewModel.updateStatus(SaveStatusRequest(nickNumber = it))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.androidDiamond.toString(),
                            icon = { StatusImage(Res.drawable.status_diamond) },
                            label = stringResource(Res.string.diamond),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(diamond = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.diamondShard.toString(),
                            icon = { StatusImage(Res.drawable.status_diamond_shd) },
                            label = stringResource(Res.string.diamond_shard),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(diamondShard = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.ap.toString(),
                            icon = { StatusImage(Res.drawable.status_ap) },
                            label = stringResource(Res.string.ap),
                            error = {
                                val int = it.toIntOrNull()
                                int == null || int !in 0 until 10000
                            },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(ap = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.gold.toString(),
                            icon = { StatusImage(Res.drawable.status_gold) },
                            label = stringResource(Res.string.gold),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(gold = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.hggShard.toString(),
                            icon = { StatusImage(Res.drawable.status_hgg_shd) },
                            label = stringResource(Res.string.hgg_shard),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(hggShard = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.lggShard.toString(),
                            icon = { StatusImage(Res.drawable.status_lgg_shd) },
                            label = stringResource(Res.string.lgg_shard),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(lggShard = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.gachaTicket.toString(),
                            icon = { StatusImage(Res.drawable.status_tkt_gacha) },
                            label = stringResource(Res.string.gacha_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(gachaTkt = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.tenGachaTicket.toString(),
                            icon = { StatusImage(Res.drawable.status_tkt_gacha_10) },
                            label = stringResource(Res.string.ten_gacha_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(tenGachaTkt = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.classicGachaTicket.toString(),
                            icon = { StatusImage(Res.drawable.status_classic_gacha) },
                            label = stringResource(Res.string.cls_gacha_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(classicGachaTkt = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.classicTenGachaTicket.toString(),
                            icon = { StatusImage(Res.drawable.status_classic_gacha_10) },
                            label = stringResource(Res.string.cls_ten_gacha_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(tenClassicGachaTkt = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.classicShard.toString(),
                            icon = { StatusImage(Res.drawable.status_classic_normal_ticket) },
                            label = stringResource(Res.string.cls_shard),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(classicShard = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.practiceTicket.toString(),
                            icon = { StatusImage(Res.drawable.status_tkt_try) },
                            label = stringResource(Res.string.try_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(tryTkt = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.recruitLicense.toString(),
                            icon = { StatusImage(Res.drawable.status_tkt_recruit) },
                            label = stringResource(Res.string.rec_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(recTkt = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.instantFinishTicket.toString(),
                            icon = { StatusImage(Res.drawable.status_tkt_inst_fin) },
                            label = stringResource(Res.string.fni_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(fniTkt = it.toInt())
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

    }
}

private fun String.checkIntRange(): Boolean {
    val int = toIntOrNull()
    return int == null || int !in 0..Int.MAX_VALUE
}

@Composable
fun LabelTextField(
    value: String = "Test",
    icon: @Composable (() -> Unit)? = null,
    label: String = "Test",
    error: (String) -> Boolean = { false },
    onValueSave: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(8.dp)
            .height(60.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray.copy(alpha = 0.9f))
            .clickable { showDialog = !showDialog }
            .padding(8.dp)
            .then(modifier)
    ) {
        if (icon != null) {
            icon()
        } else {
            Text(label, color = Color.Black)
        }
        Text(value, color = Color.Black)
    }
    if (showDialog) {
        EditTextDialog(
            value = value,
            label = label,
            error = error,
            onValueSave = {
                it?.let {
                    onValueSave(it)
                }
                showDialog = !showDialog
            }
        )
    }
}

@Composable
fun StatusImage(id: org.jetbrains.compose.resources.DrawableResource) {
    Image(
        painter = painterResource(id),
        contentDescription = null,
        modifier = Modifier.fillMaxHeight()
    )
}






