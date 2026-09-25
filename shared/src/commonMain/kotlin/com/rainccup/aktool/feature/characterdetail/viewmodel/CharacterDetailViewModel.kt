package com.rainccup.aktool.feature.characterdetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.domain.usecase.character.ChangeEvolvePhaseUseCase
import com.rainccup.aktool.core.domain.usecase.character.GetCharacterLimitsUseCase
import com.rainccup.aktool.core.domain.usecase.character.SaveCharacterUseCase
import com.rainccup.aktool.core.model.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val changeEvolvePhaseUseCase: ChangeEvolvePhaseUseCase,
    private val getCharacterLimits: GetCharacterLimitsUseCase,
    private val saveCharacter: SaveCharacterUseCase,
    charInstId: String,
) : ViewModel() {
    val charInstId: StateFlow<String>
        field = MutableStateFlow(charInstId)

    val character: StateFlow<Character>
        field = MutableStateFlow(Character.placeholder())

    val maxEvoPhase: StateFlow<Int>
        field = MutableStateFlow(2)
    val maxLevel: StateFlow<Int>
        field = MutableStateFlow(90)

    val maxSkillLevel: StateFlow<Int>
        field = MutableStateFlow(7)

    fun accept(char: Character) = viewModelScope.launch {
        character.emit(char)
    }

    /** 保存前的规则补全（模组解锁/默认选中/皮肤切换/等级满时清经验），返回补全后的角色。 */
    fun applySaveRules(char: Character): Character = saveCharacter.applyRules(char)

    fun changeEvoPhase(phase: Int) = viewModelScope.launch {
        val change = changeEvolvePhaseUseCase(character.value, phase)
        maxLevel.emit(change.maxLevel)
        maxSkillLevel.emit(change.maxSkillLevel)
        character.emit(change.character)
    }

    fun updateMaxEvoPhase(charId: String) = viewModelScope.launch {
        maxEvoPhase.emit(getCharacterLimits(charId, character.value.evolvePhase).maxEvoPhase)
    }

    fun updateMaxLevel(charId: String, phase: Int) = viewModelScope.launch {
        maxLevel.emit(getCharacterLimits(charId, phase).maxLevel)
    }

    fun updateMaxSkillLevel(phase: Int) = viewModelScope.launch {
        maxSkillLevel.emit(getCharacterLimits(character.value.charId, phase).maxSkillLevel)
    }
}
