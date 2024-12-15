package org.doctorate.aktool.ui.page.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private var _splash = MutableStateFlow(true)
    val splash = _splash.asStateFlow()

    fun closeSplash() = viewModelScope.launch {
        _splash.emit(false)
    }

    override fun onCleared() {
        super.onCleared()
        closeSplash()
    }
}