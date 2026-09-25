package com.rainccup.aktool.feature.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _splash = MutableStateFlow(true)
    val splash: StateFlow<Boolean> = _splash.asStateFlow()

    fun closeSplash() = viewModelScope.launch {
        _splash.emit(false)
    }
}
