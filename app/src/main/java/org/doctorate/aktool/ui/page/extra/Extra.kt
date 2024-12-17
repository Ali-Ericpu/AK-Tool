package org.doctorate.aktool.ui.page.extra

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import org.doctorate.aktool.R
import org.doctorate.aktool.config.Table
import org.doctorate.aktool.pojo.entity.Item
import org.doctorate.aktool.pojo.request.AddFlushMessageRequest
import org.doctorate.aktool.pojo.request.RegisterAccountRequest
import org.doctorate.aktool.pojo.request.ResetActivityRequest
import org.doctorate.aktool.pojo.request.UnlockAllCharRequest
import org.doctorate.aktool.ui.page.characterdetail.IntRangeSlider
import org.doctorate.aktool.ui.page.setting.BasicDialog
import org.doctorate.aktool.ui.page.setting.ConfirmButtonRow
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraPage() {
    val context = LocalContext.current
    val viewModel: ExtraViewModel = viewModel()
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            item {
                RequestButton(stringResource(R.string.register_acc)) {
                    viewModel.changeAccountState()
                }
                RequestButton(stringResource(R.string.unlock_all_char)) {
                    if (Table.initData(context)) {
                        viewModel.changeUnlockCharState()
                    }
                }
                RequestButton(stringResource(R.string.unlock_all_stages)) {
                    viewModel.unlockAllStages(context)
                }
                RequestButton(stringResource(R.string.unlock_all_flags)) {
                    viewModel.unlockAllFlags(context)
                }
                RequestButton(stringResource(R.string.add_flush_message)) {
                    viewModel.changeMessageState()
                }
                RequestButton(stringResource(R.string.gain_item)) {
                    viewModel.changeItemState()
                }
                RequestButton(stringResource(R.string.reset_act)) {
                    viewModel.changeActivityState()
                }
                RequestButton(stringResource(R.string.query_valid_code)) {
                    viewModel.changeValidCodeState()
                }
            }
        }
        AnimatedVisibility(
            visible = isConnecting && !showValidCodeDialog,
            enter = fadeIn(initialAlpha = 0.1f, animationSpec = tween(100)),
            exit = fadeOut(targetAlpha = 0f, animationSpec = tween(800))
        ) {
            BasicAlertDialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                val rotate by rememberInfiniteTransition(label = "").animateFloat(
                    label = "",
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                Icon(
                    painter = painterResource(R.drawable.icon),
                    null,
                    modifier = Modifier
                        .size(100.dp)
                        .rotate(rotate)
                        .align(alignment = Alignment.Center)
                )
            }
        }
    }
    if (showUnlockChar) {
        UnlockAllCharDialog {
            it?.let { viewModel.unlockAllChar(it, context) }
            viewModel.changeUnlockCharState()
        }
    }
    if (showMessageDialog) {
        AddFlushMessageDialog {
            it?.let { viewModel.addFlushMessage(it, context) }
            viewModel.changeMessageState()
        }
    }
    if (showItemDialog) {
        GainItemDialog {
            it?.let { viewModel.gainItem(it, context) }
            viewModel.changeItemState()
        }
    }
    if (showActivityDialog) {
        ResetActivityDialog {
            it?.let { viewModel.resetActivity(it, context) }
            viewModel.changeActivityState()
        }
    }
    if (showAccountDialog) {
        RegisterAccountDialog {
            it?.let { viewModel.registerAccount(it, context) }
            viewModel.changeAccountState()
        }
    }
    if (showValidCodeDialog) {
        ValidateCodeDialog(
            isRefreshing = isConnecting,
            validateCode = viewModel.validateCodeList(),
            onRefresh = { viewModel.syncValidCode(context) },
            onExit = { viewModel.changeValidCodeState() }
        )
    }
}

@Composable
fun RequestButton(
    text: String = "Test",
    onclick: () -> Unit = { }
) {
    Button(
        onClick = onclick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(
            text = text,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun UnlockAllCharDialog(onValueSave: (UnlockAllCharRequest?) -> Unit = {}) {
    var evolvePhase by remember { mutableFloatStateOf(2f) }
    var level by remember { mutableFloatStateOf(90f) }
    var maxLevel by remember { mutableFloatStateOf(90f) }
    var favorPoint by remember { mutableFloatStateOf(200f) }
    var mainSkillLvl by remember { mutableFloatStateOf(7f) }
    var equipLevel by remember { mutableFloatStateOf(3f) }
    var potentialRank by remember { mutableFloatStateOf(5f) }
    var specializeLevel by remember { mutableFloatStateOf(3f) }
    BasicAlertDialog(
        onDismissRequest = { onValueSave(null) },
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
            .wrapContentSize()
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                IntRangeSlider(
                    value = evolvePhase,
                    maxValue = 2,
                    description = stringResource(R.string.evp_phase),
                    onValueChange = { evolvePhase = it },
                    onValueChangeFinished = {
                        if (it < 1) {
                            mainSkillLvl = min(4f, mainSkillLvl)
                            specializeLevel = 0f
                            equipLevel = 1f
                        } else if (it < 2) {
                            specializeLevel = 0f
                        }
                        maxLevel = when (it) {
                            0 -> 50
                            1 -> 80
                            2 -> 90
                            else -> 90
                        }.toFloat()
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
                    description = stringResource(R.string.level),
                    onValueChange = { level = it },
                    onValueChangeFinished = { if (it > maxLevel) level = maxLevel },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                IntRangeSlider(
                    value = mainSkillLvl,
                    maxValue = 7,
                    start = 1,
                    description = stringResource(R.string.skill_level),
                    onValueChange = { mainSkillLvl = it },
                    onValueChangeFinished = {
                        if (evolvePhase.roundToInt() == 0 && it > 4) {
                            mainSkillLvl = 4f
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                IntRangeSlider(
                    value = potentialRank,
                    maxValue = 5,
                    description = stringResource(R.string.potential_rank),
                    onValueChange = { potentialRank = it },
                    modifier = Modifier.weight(1f)
                )
            }
            IntRangeSlider(
                value = specializeLevel,
                maxValue = 3,
                description = stringResource(R.string.sp_skill_lv),
                onValueChange = {
                    specializeLevel = it
                    if (evolvePhase < 2f || mainSkillLvl < 7f) {
                        specializeLevel = 0f
                    }
                },
            )
            IntRangeSlider(
                value = favorPoint,
                maxValue = 200,
                description = stringResource(R.string.fav_pt),
                onValueChange = { favorPoint = it }
            )
            IntRangeSlider(
                value = equipLevel,
                maxValue = 3,
                start = 1,
                description = stringResource(R.string.equip_lv),
                onValueChange = {
                    equipLevel = it
                    if (evolvePhase < 2) {
                        equipLevel = 1f
                    }
                },
            )
            ConfirmButtonRow(
                onCancel = { onValueSave(null) },
                onConfirm = {
                    onValueSave(
                        UnlockAllCharRequest(
                            favorPoint = Table.getRealFavPoint(favorPoint.roundToInt()),
                            potentialRank = potentialRank.roundToInt(),
                            specializeLevel = specializeLevel.roundToInt(),
                            mainSkillLvl = mainSkillLvl.roundToInt(),
                            evolvePhase = evolvePhase.roundToInt(),
                            level = level.roundToInt(),
                            equipLevel = equipLevel.roundToInt(),
                            enableRogueChar = false
                        )
                    )
                }
            )
        }
    }
}


@Composable
private fun AddFlushMessageDialog(onValueSave: (AddFlushMessageRequest?) -> Unit = {}) {
    val context = LocalContext.current
    var uid by remember { mutableStateOf("ALL") }
    var message by remember { mutableStateOf("") }
    val error = message.isEmpty()
    BasicDialog(
        error = error,
        label = stringResource(R.string.push_message),
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (error) {
                Toast.makeText(context, R.string.error_data, Toast.LENGTH_SHORT).show()
            } else {
                onValueSave(AddFlushMessageRequest(uid, message))
            }
        }
    ) {
        OutlinedTextField(
            value = uid,
            label = { Text(stringResource(R.string.uid)) },
            singleLine = true,
            onValueChange = { uid = it },
        )
        OutlinedTextField(
            value = message,
            label = { Text(stringResource(R.string.message)) },
            maxLines = Int.MAX_VALUE,
            onValueChange = { message = it },
        )
    }
}

@Composable
private fun ResetActivityDialog(onValueSave: (ResetActivityRequest?) -> Unit = {}) {
    val context = LocalContext.current
    var id by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    val error = id.isEmpty() || type.isEmpty()
    BasicDialog(
        error = error,
        label = stringResource(R.string.reset_act),
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (error) {
                Toast.makeText(context, R.string.error_data, Toast.LENGTH_SHORT).show()
            } else {
                onValueSave(ResetActivityRequest(type, id))
            }
        }
    ) {
        OutlinedTextField(
            value = type,
            label = { Text(stringResource(R.string.act_type)) },
            singleLine = true,
            onValueChange = { type = it },
        )
        OutlinedTextField(
            value = id,
            label = { Text(stringResource(R.string.act_id)) },
            singleLine = true,
            onValueChange = { id = it },
        )
    }
}

@Composable
private fun GainItemDialog(onValueSave: (Item?) -> Unit = {}) {
    val context = LocalContext.current
    var itemId by remember { mutableStateOf("") }
    var itemType by remember { mutableStateOf("") }
    var count by remember { mutableFloatStateOf(1f) }
    val error = itemId.isEmpty() || itemType.isEmpty()
    BasicDialog(
        error = error,
        label = stringResource(R.string.item),
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (error) {
                Toast.makeText(context, R.string.error_data, Toast.LENGTH_SHORT).show()
            } else {
                onValueSave(Item(itemId, itemType, count.roundToInt()))
            }
        }
    ) {
        OutlinedTextField(
            value = itemId,
            label = { Text(stringResource(R.string.item_id)) },
            singleLine = true,
            onValueChange = { itemId = it },
        )
        OutlinedTextField(
            value = itemType,
            label = { Text(stringResource(R.string.item_type)) },
            maxLines = Int.MAX_VALUE,
            onValueChange = { itemType = it },
        )
        IntRangeSlider(
            value = count,
            start = 1,
            maxValue = 99,
            textColor = MaterialTheme.colorScheme.onBackground,
            color = Color.Unspecified,
            description = stringResource(R.string.count),
            onValueChange = { count = it },
            modifier = Modifier
                .padding(4.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

@Composable
private fun RegisterAccountDialog(onValueSave: (RegisterAccountRequest?) -> Unit = {}) {
    val context = LocalContext.current
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error = account.toULongOrNull() == null || account.length > 11 || password.isEmpty()
    BasicDialog(
        error = error,
        label = stringResource(R.string.register_acc),
        onCancel = { onValueSave(null) },
        onConfirm = {
            if (error) {
                Toast.makeText(context, R.string.error_data, Toast.LENGTH_SHORT).show()
            } else {
                onValueSave(RegisterAccountRequest(account, password))
            }
        }
    ) {
        OutlinedTextField(
            value = account,
            label = { Text(stringResource(R.string.account)) },
            singleLine = true,
            onValueChange = { account = it },
        )
        OutlinedTextField(
            value = password,
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            onValueChange = { password = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ValidateCodeDialog(
    isRefreshing: Boolean = false,
    validateCode: List<Pair<String, String>> = listOf(
        "31231" to "321312",
        "3123311" to "3211312",
        "3123131" to "32123312",
    ),
    onRefresh: () -> Unit = { },
    onExit: () -> Unit = { }
) {
    val context = LocalContext.current
    val manager = LocalClipboardManager.current
    LaunchedEffect(Unit) { onRefresh() }
    BasicAlertDialog(
        onDismissRequest = { onExit() },
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh() }
        ) {
            if (validateCode.isEmpty()) {
                Text(
                    text = stringResource(R.string.is_nothing),
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            LazyColumn(
                modifier = Modifier.height(260.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.valid_code),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    )
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
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp)
                    ) {
                        val message = stringResource(R.string.copy_success)
                        Text(account, fontSize = 24.sp)
                        IconButton(
                            onClick = {
                                manager.setText(AnnotatedString(code))
                                Toast.makeText(context, message.format(code), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxHeight()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_copy),
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}