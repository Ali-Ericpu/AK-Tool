package com.rainccup.aktool.feature.extra.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rainccup.aktool.core.designsystem.component.BasicDialog
import com.rainccup.aktool.core.designsystem.component.ConfirmButtonRow
import com.rainccup.aktool.core.designsystem.component.IntRangeSlider
import com.rainccup.aktool.core.designsystem.component.roundedBackground
import com.rainccup.aktool.core.domain.model.UnlockAllCharRules
import com.rainccup.aktool.core.model.AddFlushMessageRequest
import com.rainccup.aktool.core.model.Item
import com.rainccup.aktool.core.model.RegisterAccountRequest
import com.rainccup.aktool.core.model.ResetActivityRequest
import com.rainccup.aktool.core.model.UnlockAllCharRequest
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.feature.extra.viewmodel.ExtraViewModel
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.account
import com.rainccup.aktool.resources.act_id
import com.rainccup.aktool.resources.act_type
import com.rainccup.aktool.resources.add_flush_message
import com.rainccup.aktool.resources.baseline_copy
import com.rainccup.aktool.resources.content_invalid
import com.rainccup.aktool.resources.copy_success
import com.rainccup.aktool.resources.count
import com.rainccup.aktool.resources.equip_lv
import com.rainccup.aktool.resources.evp_phase
import com.rainccup.aktool.resources.fav_pt
import com.rainccup.aktool.resources.gain_item
import com.rainccup.aktool.resources.is_nothing
import com.rainccup.aktool.resources.item
import com.rainccup.aktool.resources.item_id
import com.rainccup.aktool.resources.item_type
import com.rainccup.aktool.resources.level
import com.rainccup.aktool.resources.message
import com.rainccup.aktool.resources.password
import com.rainccup.aktool.resources.potential_rank
import com.rainccup.aktool.resources.push_message
import com.rainccup.aktool.resources.query_account
import com.rainccup.aktool.resources.query_valid_code
import com.rainccup.aktool.resources.register_acc
import com.rainccup.aktool.resources.reset_account
import com.rainccup.aktool.resources.reset_act
import com.rainccup.aktool.resources.reset_rlv2
import com.rainccup.aktool.resources.skill_level
import com.rainccup.aktool.resources.sp_skill_lv
import com.rainccup.aktool.resources.uid
import com.rainccup.aktool.resources.unlock_all_char
import com.rainccup.aktool.resources.unlock_all_flags
import com.rainccup.aktool.resources.unlock_all_stages
import com.rainccup.aktool.resources.valid_code
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ExtraPage() {
    val viewModel: ExtraViewModel = koinViewModel()
    val showUnlockChar by viewModel.showUnlockChar.collectAsState()
    val showMessageDialog by viewModel.showMessageDialog.collectAsState()
    val showItemDialog by viewModel.showItemDialog.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val showActivityDialog by viewModel.showActivityDialog.collectAsState()
    val showAccountDialog by viewModel.showAccountDialog.collectAsState()
    val showValidCodeDialog by viewModel.showValidCodeDialog.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {

            item {
                Column(modifier = Modifier.roundedBackground()) {
                    ArrowPreference(
                        title = stringResource(Res.string.register_acc),
                        onClick = viewModel::changeAccountState
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.query_account),
                        onClick = viewModel::queryAccountByUID
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.reset_account),
                        onClick = viewModel::resetAccount
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.add_flush_message),
                        onClick = viewModel::changeMessageState
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.query_valid_code),
                        onClick = viewModel::changeValidCodeState
                    )
                }
            }
            item {
                Column(modifier = Modifier.roundedBackground()) {
                    ArrowPreference(
                        title = stringResource(Res.string.unlock_all_char),
                        onClick = viewModel::changeUnlockCharState
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.unlock_all_stages),
                        onClick = viewModel::unlockAllStages
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.unlock_all_flags),
                        onClick = viewModel::unlockAllFlags
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.gain_item),
                        onClick = viewModel::changeItemState
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.reset_act),
                        onClick = viewModel::changeActivityState
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.reset_rlv2),
                        onClick = viewModel::resetRlv2
                    )
                }
            }
        }
    }
    UnlockAllCharDialog(showUnlockChar) {
        it?.let { viewModel.unlockAllChar(it) }
        viewModel.changeUnlockCharState()
    }
    AddFlushMessageDialog(showMessageDialog) {
        it?.let { viewModel.addFlushMessage(it) }
        viewModel.changeMessageState()
    }
    GainItemDialog(showItemDialog) {
        it?.let { viewModel.gainItem(it) }
        viewModel.changeItemState()
    }
    ResetActivityDialog(showActivityDialog) {
        it?.let { viewModel.resetActivity(it) }
        viewModel.changeActivityState()
    }
    RegisterAccountDialog(showAccountDialog) {
        it?.let { viewModel.registerAccount(it) }
        viewModel.changeAccountState()
    }
    ValidateCodeDialog(
        show = showValidCodeDialog,
        isRefreshing = isConnecting,
        validateCode = viewModel.validateCodeList(),
        onRefresh = viewModel::syncValidCode,
        onExit = viewModel::changeValidCodeState
    )
}

@Composable
private fun UnlockAllCharDialog(
    show: Boolean,
    onValueSave: (UnlockAllCharRequest?) -> Unit = {}
) {
    var evolvePhase by remember { mutableIntStateOf(2) }
    var level by remember { mutableIntStateOf(90) }
    var maxLevel by remember { mutableIntStateOf(90) }
    var favorPoint by remember { mutableIntStateOf(200) }
    var mainSkillLvl by remember { mutableIntStateOf(7) }
    var equipLevel by remember { mutableIntStateOf(3) }
    var potentialRank by remember { mutableIntStateOf(5) }
    var specializeLevel by remember { mutableIntStateOf(3) }
    OverlayDialog(
        title = stringResource(Res.string.unlock_all_char),
        show = show,
        onDismissRequest = { onValueSave(null) },
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                IntRangeSlider(
                    value = evolvePhase,
                    maxValue = 2,
                    title = stringResource(Res.string.evp_phase),
//                    onValueChange = { evolvePhase = it },
                    onValueChangeFinished = {
                        val effects = UnlockAllCharRules.onEvolvePhaseChanged(
                            phase = it,
                            mainSkillLvl = mainSkillLvl,
                            specializeLevel = specializeLevel,
                            equipLevel = equipLevel,
                        )
                        mainSkillLvl = effects.mainSkillLvl
                        specializeLevel = effects.specializeLevel
                        equipLevel = effects.equipLevel
                        maxLevel = effects.maxLevel
                        if (level > maxLevel) {
                            level = maxLevel
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                IntRangeSlider(
                    value = level,
                    maxValue = 90,
                    start = 1,
                    title = stringResource(Res.string.level),
//                    onValueChange = { level = it },
                    onValueChangeFinished = {
                        level = it
                        if (it > maxLevel) level = maxLevel
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                IntRangeSlider(
                    value = mainSkillLvl,
                    maxValue = 7,
                    start = 1,
                    title = stringResource(Res.string.skill_level),
                    onValueChangeFinished = {
                        mainSkillLvl = UnlockAllCharRules.clampSkillLevel(evolvePhase, it)
                    },
                    modifier = Modifier.weight(1f)
                )
                IntRangeSlider(
                    value = potentialRank,
                    maxValue = 5,
                    title = stringResource(Res.string.potential_rank),
                    onValueChangeFinished = { potentialRank = it },
                    modifier = Modifier.weight(1f)
                )
            }
            IntRangeSlider(
                value = specializeLevel,
                maxValue = 3,
                title = stringResource(Res.string.sp_skill_lv),
                onValueChangeFinished = {
                    specializeLevel =
                        UnlockAllCharRules.normalizeSpecializeLevel(evolvePhase, mainSkillLvl, it)
                },
            )
            IntRangeSlider(
                value = favorPoint,
                maxValue = 200,
                title = stringResource(Res.string.fav_pt),
                onValueChangeFinished = { favorPoint = it }
            )
            IntRangeSlider(
                value = equipLevel,
                maxValue = 3,
                start = 1,
                title = stringResource(Res.string.equip_lv),
                onValueChangeFinished = {
                    equipLevel = UnlockAllCharRules.normalizeEquipLevel(evolvePhase, it)
                },
            )
            ConfirmButtonRow(
                onCancel = { onValueSave(null) },
                onConfirm = {
                    onValueSave(
                        UnlockAllCharRequest(
                            favorPoint = favorPoint,
                            potentialRank = potentialRank,
                            specializeLevel = specializeLevel,
                            mainSkillLvl = mainSkillLvl,
                            evolvePhase = evolvePhase,
                            level = level,
                            equipLevel = equipLevel,
                            enableRogueChar = false
                        )
                    )
                }
            )
        }
    }
}


@Composable
private fun AddFlushMessageDialog(
    show: Boolean,
    onValueSave: (AddFlushMessageRequest?) -> Unit = {}
) {
    var uid by remember { mutableStateOf("ALL") }
    var message by remember { mutableStateOf("") }
    val error = message.isEmpty() || uid.isEmpty()
    BasicDialog(
        show = show,
        error = error,
        summary = if (error) stringResource(Res.string.content_invalid) else null,
        summaryColor = if (error) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onBackground,
        title = stringResource(Res.string.push_message),
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (error) {

            } else {
                onValueSave(AddFlushMessageRequest(uid, message))
            }
        }
    ) {
        TextField(
            value = uid,
            onValueChange = { uid = it },
            label = stringResource(Res.string.uid),
            singleLine = true,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = message,
            onValueChange = { message = it },
            label = stringResource(Res.string.message),
            maxLines = Int.MAX_VALUE,
        )
    }
}

@Composable
private fun ResetActivityDialog(
    show: Boolean,
    onValueSave: (ResetActivityRequest?) -> Unit = {}
) {
    var id by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    val error = id.isEmpty() || type.isEmpty()
    BasicDialog(
        show = show,
        error = error,
        summary = if (error) stringResource(Res.string.content_invalid) else null,
        summaryColor = if (error) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onBackground,
        title = stringResource(Res.string.reset_act),
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (error) {

            } else {
                onValueSave(ResetActivityRequest(type, id))
            }
        }
    ) {
        TextField(
            value = type,
            onValueChange = { type = it },
            label = stringResource(Res.string.act_type),
            singleLine = true,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = id,
            onValueChange = { id = it },
            label = stringResource(Res.string.act_id),
            singleLine = true,
        )
    }
}

@Composable
private fun GainItemDialog(
    show: Boolean,
    onValueSave: (Item?) -> Unit = {}
) {
    var itemId by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("") }
    var count by remember { mutableIntStateOf(1) }
    val error = itemId.isEmpty() || itemType.isEmpty()
    BasicDialog(
        title = stringResource(Res.string.item),
        show = show,
        summary = if (error) stringResource(Res.string.content_invalid) else null,
        summaryColor = if (error) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onBackground,
        error = error,
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (error) {

            } else {
                onValueSave(Item(itemId, itemType, count))
            }
        }
    ) {
        TextField(
            value = itemId,
            onValueChange = { itemId = it },
            label = stringResource(Res.string.item_id),
            singleLine = true,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = itemType,
            onValueChange = { itemType = it },
            label = stringResource(Res.string.item_type),
            maxLines = Int.MAX_VALUE,
        )
        IntRangeSlider(
            value = count,
            start = 1,
            maxValue = 99,
            title = stringResource(Res.string.count),
            onValueChangeFinished = { count = it },
        )
    }
}

@Composable
private fun RegisterAccountDialog(
    show: Boolean,
    onValueSave: (RegisterAccountRequest?) -> Unit = {}
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error = account.isBlank() || account.length > 15 || password.isBlank()
    BasicDialog(
        show = show,
        title = stringResource(Res.string.register_acc),
        error = error,
        summary = if (error) stringResource(Res.string.content_invalid) else null,
        summaryColor = if (error) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onBackground,
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (error) {

            } else {
                onValueSave(RegisterAccountRequest(account, password))
            }
        }
    ) {
        TextField(
            value = account,
            onValueChange = { account = it },
            label = stringResource(Res.string.account),
            singleLine = true,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(Res.string.password),
            singleLine = true,
        )
    }
}

@Composable
private fun ValidateCodeDialog(
    show: Boolean,
    isRefreshing: Boolean = false,
    validateCode: List<Pair<String, String>> = listOf(),
    onRefresh: () -> Unit = { },
    onExit: () -> Unit = { }
) {
    val clipboard = koinInject<ClipboardPort>()
    SideEffect(show) {
        if (show) {
            onRefresh()
        }
    }
    OverlayDialog(
        show = show,
        onDismissRequest = { onExit() },
    ) {
        Column {
            Text(
                text = stringResource(Res.string.valid_code),
                color = MiuixTheme.colorScheme.onBackground,
                style = MiuixTheme.textStyles.title1,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
            )
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { onRefresh() },
            ) {
                LazyColumn(modifier = Modifier.height(240.dp)) {
                    item {
                        if (validateCode.isEmpty()) {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                Text(
                                    text = stringResource(Res.string.is_nothing),
                                    fontSize = 24.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                    items(validateCode) { (account, code) ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = MiuixTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(4.dp)
                        ) {
                            val message = stringResource(Res.string.copy_success)
                            Text(account, fontSize = 24.sp)
                            IconButton(
                                onClick = { clipboard.setText(code) },
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.baseline_copy),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MiuixTheme.colorScheme.primary)
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}







