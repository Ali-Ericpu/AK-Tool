package com.rainccup.aktool.feature.splash.viewmodel

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val splashModule = module {
    viewModel { SplashViewModel() }
}
