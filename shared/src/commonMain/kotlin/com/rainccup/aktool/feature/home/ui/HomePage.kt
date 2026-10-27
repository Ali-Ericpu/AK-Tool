package com.rainccup.aktool.feature.home.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.rainccup.aktool.core.common.validation.checkIntRange
import com.rainccup.aktool.core.common.validation.coerceToValid
import com.rainccup.aktool.core.designsystem.LocalFloatingBarInset
import com.rainccup.aktool.core.designsystem.LocalLargeScreen
import com.rainccup.aktool.core.designsystem.anim.itemEnterAnimation
import com.rainccup.aktool.core.designsystem.component.BirthdayPickerDialog
import com.rainccup.aktool.core.designsystem.component.EditTextDialog
import com.rainccup.aktool.core.model.Birthday
import com.rainccup.aktool.core.model.SaveStatusRequest
import com.rainccup.aktool.feature.home.viewmodel.HomeViewModel
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.ap
import com.rainccup.aktool.resources.baseline_home_start
import com.rainccup.aktool.resources.birthday
import com.rainccup.aktool.resources.birthday_format
import com.rainccup.aktool.resources.cls_gacha_tkt
import com.rainccup.aktool.resources.cls_shard
import com.rainccup.aktool.resources.cls_ten_gacha_tkt
import com.rainccup.aktool.resources.diamond
import com.rainccup.aktool.resources.diamond_shard
import com.rainccup.aktool.resources.fni_tkt
import com.rainccup.aktool.resources.gacha_tkt
import com.rainccup.aktool.resources.gold
import com.rainccup.aktool.resources.hgg_shard
import com.rainccup.aktool.resources.level
import com.rainccup.aktool.resources.lgg_shard
import com.rainccup.aktool.resources.nick_name
import com.rainccup.aktool.resources.nick_num
import com.rainccup.aktool.resources.rec_tkt
import com.rainccup.aktool.resources.status_ap
import com.rainccup.aktool.resources.status_classic_gacha
import com.rainccup.aktool.resources.status_classic_gacha_10
import com.rainccup.aktool.resources.status_classic_normal_ticket
import com.rainccup.aktool.resources.status_diamond
import com.rainccup.aktool.resources.status_diamond_shd
import com.rainccup.aktool.resources.status_gold
import com.rainccup.aktool.resources.status_hgg_shd
import com.rainccup.aktool.resources.status_lgg_shd
import com.rainccup.aktool.resources.status_tkt_gacha
import com.rainccup.aktool.resources.status_tkt_gacha_10
import com.rainccup.aktool.resources.status_tkt_inst_fin
import com.rainccup.aktool.resources.status_tkt_recruit
import com.rainccup.aktool.resources.status_tkt_try
import com.rainccup.aktool.resources.ten_gacha_tkt
import com.rainccup.aktool.resources.try_tkt
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomePage() {
    val viewModel: HomeViewModel = koinViewModel()
    val isSplash by viewModel.isSplash.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val status by viewModel.status.collectAsState()
    val twoPerRow = LocalLargeScreen.current
    val resourceCellModifier = Modifier.fillMaxWidth(if (twoPerRow) 0.49f else 1f)
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
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
            Box {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // 内容直接在悬浮底栏底下滑过去，最后一项靠这段底部留白停在浮层之上。
                    contentPadding = PaddingValues(bottom = LocalFloatingBarInset.current),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .itemEnterAnimation(index = 0)
                                .background(
                                    MiuixTheme.colorScheme.background.copy(alpha = 0.95f),
                                    RoundedCornerShape(16.dp)
                                )
                        ) {
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
                            BirthdayRow(
                                value = status.birthday,
                                label = stringResource(Res.string.birthday),
                                onValueSave = {
                                    viewModel.updateStatus(SaveStatusRequest(birthday = it))
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
                                        viewModel.updateStatus(SaveStatusRequest(level = it.toInt()))
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
                        }
                    }
                    item {
                        // 资源项：大屏下每排两个；窄屏 maxItemsInEachRow = 1 退化成原来的竖排。
                        //
                        // 这里刻意**不用** Modifier.weight：FlowRow 对带权重的子项会改去量 intrinsic 尺寸
                        // （见 FlowLayout.measureAndCache 的注释），而 miuix 的 ArrowPreference 被问
                        // maxIntrinsicHeight 时会构造出非法 Constraints，直接抛
                        // "maxWidth must be >= than minWidth" 把应用搞崩。
                        // 改用"占行宽的比例"，FlowRow 就会走 measure(constraints) 的正常测量路径。
                        FlowRow(
                            maxItemsInEachRow = if (twoPerRow) 2 else 1,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .itemEnterAnimation(index = 1)
                                .background(
                                    color = MiuixTheme.colorScheme.background.copy(alpha = 0.95f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            LabelTextField(
                                value = status.androidDiamond.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_diamond) },
                                label = stringResource(Res.string.diamond),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(diamond = it.toInt())
                                    )
                                }
                            )
                            LabelTextField(
                                value = status.diamondShard.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_diamond_shd) },
                                label = stringResource(Res.string.diamond_shard),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(diamondShard = it.toInt())
                                    )
                                }
                            )
                            LabelTextField(
                                value = status.ap.toString(),
                                modifier = resourceCellModifier,
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
                            )
                            LabelTextField(
                                value = status.gold.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_gold) },
                                label = stringResource(Res.string.gold),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(gold = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.hggShard.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_hgg_shd) },
                                label = stringResource(Res.string.hgg_shard),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(hggShard = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.lggShard.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_lgg_shd) },
                                label = stringResource(Res.string.lgg_shard),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(lggShard = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.gachaTicket.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_tkt_gacha) },
                                label = stringResource(Res.string.gacha_tkt),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(gachaTkt = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.tenGachaTicket.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_tkt_gacha_10) },
                                label = stringResource(Res.string.ten_gacha_tkt),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(tenGachaTkt = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.classicGachaTicket.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_classic_gacha) },
                                label = stringResource(Res.string.cls_gacha_tkt),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(classicGachaTkt = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.classicTenGachaTicket.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_classic_gacha_10) },
                                label = stringResource(Res.string.cls_ten_gacha_tkt),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(tenClassicGachaTkt = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.classicShard.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_classic_normal_ticket) },
                                label = stringResource(Res.string.cls_shard),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(classicShard = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.practiceTicket.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_tkt_try) },
                                label = stringResource(Res.string.try_tkt),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(tryTkt = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.recruitLicense.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_tkt_recruit) },
                                label = stringResource(Res.string.rec_tkt),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(recTkt = it.toInt())
                                    )
                                },
                            )
                            LabelTextField(
                                value = status.instantFinishTicket.toString(),
                                modifier = resourceCellModifier,
                                icon = { StatusImage(Res.drawable.status_tkt_inst_fin) },
                                label = stringResource(Res.string.fni_tkt),
                                error = { it.checkIntRange() },
                                onValueSave = {
                                    viewModel.updateStatus(
                                        SaveStatusRequest(fniTkt = it.toInt())
                                    )
                                },
                            )
                        }
                    }
                }
            }
            IconButton(
                onClick = viewModel::refresh,
                backgroundColor = MiuixTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp)
                    // 悬浮底栏是叠层，按钮得抬到它上方；大屏走左侧导航栏时留白为 0。
                    .padding(bottom = LocalFloatingBarInset.current)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp)
                )
            }
        }

    }
}

@Composable
fun LabelTextField(
    value: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    label: String = "Test",
    error: (String) -> Boolean = { false },
    onValueSave: (String) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    ArrowPreference(
        title = label,
        endActions = { Text(text = value, color = MiuixTheme.colorScheme.onBackground) },
        startAction = icon,
        onClick = { showDialog = !showDialog },
        holdDownState = showDialog,
        modifier = modifier,
    )
    EditTextDialog(
        value = value,
        show = showDialog,
        title = label,
        error = error,
        onConfirm = {
            it?.let { onValueSave(it) }
            showDialog = !showDialog
        }
    )
}

/**
 * 生日条目：与 [LabelTextField] 同款的 ArrowPreference 行，但点击后弹出的是日期选择器
 * 而不是输入框。值为空（服务端未设置）时显示占位文案。
 */
@Composable
fun BirthdayRow(
    value: Birthday?,
    label: String,
    modifier: Modifier = Modifier,
    onValueSave: (Birthday) -> Unit = {},
) {
    var showDialog by remember { mutableStateOf(false) }
    ArrowPreference(
        title = label,
        endActions = {
            Text(
                text = value?.coerceToValid()
                    ?.let { stringResource(Res.string.birthday_format, it.month, it.day) }
                    ?: "-",
                color = MiuixTheme.colorScheme.onBackground,
            )
        },
        onClick = { showDialog = !showDialog },
        holdDownState = showDialog,
        modifier = modifier,
    )
    BirthdayPickerDialog(
        show = showDialog,
        birthday = value,
        title = label,
        onConfirm = {
            onValueSave(it)
            showDialog = !showDialog
        },
        onCancel = { showDialog = !showDialog },
    )
}

@Composable
fun StatusImage(id: DrawableResource) {
    Image(
        painter = painterResource(id),
        contentDescription = null,
        modifier = Modifier.size(28.dp)
    )
}






