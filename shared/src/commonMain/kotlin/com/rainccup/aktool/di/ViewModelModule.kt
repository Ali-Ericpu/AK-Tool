package com.rainccup.aktool.di

import com.rainccup.aktool.ui.character.CharacterViewModel
import com.rainccup.aktool.ui.characterdetail.CharacterDetailViewModel
import com.rainccup.aktool.ui.extra.ExtraViewModel
import com.rainccup.aktool.ui.home.HomeViewModel
import com.rainccup.aktool.ui.setting.SettingViewModel
import com.rainccup.aktool.ui.splash.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SplashViewModel() }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { CharacterViewModel(get(), get(), get()) }
    viewModel { (charInstId: String) -> CharacterDetailViewModel(get(), charInstId) }
    viewModel { ExtraViewModel(get(), get(), get()) }
    viewModel { SettingViewModel(get(), get(), get(), get()) }
}
