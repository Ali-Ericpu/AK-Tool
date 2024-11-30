package org.doctorate.aktool.ui.page.setting

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.doctorate.aktool.config.Table

class SettingViewModel: ViewModel() {

    private var _isUpdateExcel = MutableStateFlow(false)
    val isUpdateExcel = _isUpdateExcel.asStateFlow()

    suspend fun updateExcel(context:Context, uri: String) {
        _isUpdateExcel.emit(true)
        runCatching { Table.refreshData(context, uri) }.onSuccess {
            Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Log.d("Update_Excel", "updateExcelFail: ${it.message}")
            Toast.makeText(context, "更新失败", Toast.LENGTH_SHORT).show()
        }
        _isUpdateExcel.emit(false)
    }
}