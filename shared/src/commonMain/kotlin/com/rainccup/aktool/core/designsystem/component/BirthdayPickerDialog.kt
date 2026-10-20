package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rainccup.aktool.core.common.validation.coerceToValid
import com.rainccup.aktool.core.common.validation.daysInMonth
import com.rainccup.aktool.core.model.Birthday
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.Text

/**
 * 生日选择弹窗：两列 Miuix [NumberPicker]（月 / 日），只允许合法日期通过。
 *
 * 合法性由三层保证，不依赖弹窗自身的交互：
 * 1. 「日」的可选项随「月」变化（[daysInMonth]），滚不出 2 月 31 日；
 * 2. 切月时同步把「日」夹回新月范围内，不会出现中间态的非法组合；
 *
 * `Birthday` 没有年份，闰年无从判断，所以 2 月按 29 天处理。
 */
@Composable
fun BirthdayPickerDialog(
    show: Boolean,
    birthday: Birthday?,
    title: String,
    onConfirm: (Birthday) -> Unit = { },
    onCancel: () -> Unit = { },
) {
    // 每次打开都从当前值重建草稿：关闭后再开不会留下上次未提交的修改。
    // 服务端可能回 2/30 这类脏数据，先夹回合法范围，否则 NumberPicker 的初值就错位。
    val initial = remember(show) { birthday?.coerceToValid() ?: Birthday(1, 1) }
    var month by remember(show) { mutableIntStateOf(initial.month) }
    var day by remember(show) { mutableIntStateOf(initial.day) }

    BasicDialog(
        title = title,
        show = show,
        onCancel = onCancel,
        onConfirm = {
            onConfirm(Birthday(month, day))
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            NumberPicker(
                value = month,
                label = { it.toString().padStart(2, '0') },
                range = 1..12,
                wrapAround = true,
                onValueChange = { picked ->
                    month = picked
                    // 同步收缩，避免「日」停在新月根本不存在的一天
                    val limit = daysInMonth(picked)
                    if (day > limit) day = limit
                },
                modifier = Modifier.weight(1f)
            )
            Text("/")
            NumberPicker(
                value = day,
                label = { it.toString().padStart(2, '0') },
                range = 1..daysInMonth(month),
                wrapAround = true,
                onValueChange = { day = it },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
