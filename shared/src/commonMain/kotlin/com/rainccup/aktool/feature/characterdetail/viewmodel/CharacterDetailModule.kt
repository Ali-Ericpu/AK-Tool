package com.rainccup.aktool.feature.characterdetail.viewmodel

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val characterDetailModule = module {
    viewModel { (charInstId: String) -> CharacterDetailViewModel(get(), get(), get(), charInstId) }
}
