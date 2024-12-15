package org.doctorate.aktool.ui.page.home

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.doctorate.aktool.R
import org.doctorate.aktool.pojo.entity.Status
import org.doctorate.aktool.pojo.request.SaveStatusRequest
import org.doctorate.aktool.ui.page.setting.EditTextDialog

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel()
    val isSplash by viewModel.isSplash.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val status: Status by viewModel.status.collectAsState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh(context) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (isSplash) {
            if (!isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.refresh(context) },
                        modifier = Modifier
                            .padding(4.dp)
                            .size(72.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_home_start),
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
                        label = stringResource(R.string.nick_name),
                        error = { text ->
                            text.toCharArray().sumOf { if (it.code > 255) 2L else 1L } > 16
                        },
                        onValueSave = {
                            viewModel.updateStatus(SaveStatusRequest(nickName = it), context)
                        },
                    )
                    Row {
                        LabelTextField(
                            value = status.level.toString(),
                            label = stringResource(R.string.level),
                            error = {
                                val int = it.toIntOrNull()
                                int == null || int !in 1..120
                            },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(level = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.nickNumber,
                            label = stringResource(R.string.nick_num),
                            error = { text ->
                                text.length != 4 || text.any { it.code !in 48..57 }
                            },
                            onValueSave = {
                                viewModel.updateStatus(SaveStatusRequest(nickNumber = it), context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.androidDiamond.toString(),
                            icon = { StatusImage(R.drawable.status_diamond) },
                            label = stringResource(R.string.diamond),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(diamond = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.diamondShard.toString(),
                            icon = { StatusImage(R.drawable.status_diamond_shd) },
                            label = stringResource(R.string.diamond_shard),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(diamondShard = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.ap.toString(),
                            icon = { StatusImage(R.drawable.status_ap) },
                            label = stringResource(R.string.ap),
                            error = {
                                val int = it.toIntOrNull()
                                int == null || int !in 0 until 10000
                            },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(ap = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.gold.toString(),
                            icon = { StatusImage(R.drawable.status_gold) },
                            label = stringResource(R.string.gold),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(gold = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.hggShard.toString(),
                            icon = { StatusImage(R.drawable.status_hgg_shd) },
                            label = stringResource(R.string.hgg_shard),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(hggShard = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.lggShard.toString(),
                            icon = { StatusImage(R.drawable.status_lgg_shd) },
                            label = stringResource(R.string.lgg_shard),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(lggShard = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.gachaTicket.toString(),
                            icon = { StatusImage(R.drawable.status_tkt_gacha) },
                            label = stringResource(R.string.gacha_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(gachaTkt = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.tenGachaTicket.toString(),
                            icon = { StatusImage(R.drawable.status_tkt_gacha_10) },
                            label = stringResource(R.string.ten_gacha_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(tenGachaTkt = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.classicGachaTicket.toString(),
                            icon = { StatusImage(R.drawable.status_classic_gacha) },
                            label = stringResource(R.string.cls_gacha_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(classicGachaTkt = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.classicTenGachaTicket.toString(),
                            icon = { StatusImage(R.drawable.status_classic_gacha_10) },
                            label = stringResource(R.string.cls_ten_gacha_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(tenClassicGachaTkt = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.classicShard.toString(),
                            icon = { StatusImage(R.drawable.status_classic_normal_ticket) },
                            label = stringResource(R.string.cls_shard),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(classicShard = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.practiceTicket.toString(),
                            icon = { StatusImage(R.drawable.status_tkt_try) },
                            label = stringResource(R.string.try_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(tryTkt = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row {
                        LabelTextField(
                            value = status.recruitLicense.toString(),
                            icon = { StatusImage(R.drawable.status_tkt_recruit) },
                            label = stringResource(R.string.rec_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(recTkt = it.toInt()), context
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        LabelTextField(
                            value = status.instantFinishTicket.toString(),
                            icon = { StatusImage(R.drawable.status_tkt_inst_fin) },
                            label = stringResource(R.string.fni_tkt),
                            error = { it.checkIntRange() },
                            onValueSave = {
                                viewModel.updateStatus(
                                    SaveStatusRequest(fniTkt = it.toInt()), context
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

@Preview
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
fun StatusImage(@DrawableRes id: Int) {
    Image(
        painter = painterResource(id),
        null,
        modifier = Modifier.fillMaxHeight()
    )
}

