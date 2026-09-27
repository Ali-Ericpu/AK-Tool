package com.rainccup.aktool.feature.setting.viewmodel

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingModule = module {
    viewModel { SettingViewModel(get(), get()) }
}
