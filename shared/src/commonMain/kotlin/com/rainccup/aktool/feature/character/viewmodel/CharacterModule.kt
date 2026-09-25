package com.rainccup.aktool.feature.character.viewmodel

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val characterModule = module {
    viewModel { CharacterViewModel(get(), get(), get(), get()) }
}
