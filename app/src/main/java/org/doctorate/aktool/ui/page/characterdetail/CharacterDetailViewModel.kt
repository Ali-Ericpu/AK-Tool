package org.doctorate.aktool.ui.page.characterdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CharacterDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val charInstId = savedStateHandle.getStateFlow("char_inst_id", "0")

}