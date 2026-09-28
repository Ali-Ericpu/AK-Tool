package com.rainccup.aktool.feature.extra.viewmodel

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val extraModule = module {
    viewModel { ExtraViewModel(get(), get(), get()) }
}
