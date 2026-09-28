package com.rainccup.aktool.feature.characterdetail.viewmodel

import com.rainccup.aktool.core.model.Character
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val characterDetailModule = module {
    viewModel { (char: Character) -> CharacterDetailViewModel(get(), get(), char) }
}
