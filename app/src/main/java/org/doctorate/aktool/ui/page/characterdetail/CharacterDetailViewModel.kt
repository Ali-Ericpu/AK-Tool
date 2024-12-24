package org.doctorate.aktool.ui.page.characterdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.doctorate.aktool.config.Table
import org.doctorate.aktool.pojo.entity.Character
import kotlin.math.min

class CharacterDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val charInstId = savedStateHandle.getStateFlow("char_inst_id", "0")

    private var _char = MutableStateFlow(Character.char())
    val char: StateFlow<Character> = _char.asStateFlow()

    private var _maxEvoPhase = MutableStateFlow(2)
    val maxEvoPhase = _maxEvoPhase.asStateFlow()

    private var _maxLevel = MutableStateFlow(90)
    val maxLevel = _maxLevel.asStateFlow()

    private var _maxSkillLevel = MutableStateFlow(7)
    val maxSkillLevel = _maxSkillLevel.asStateFlow()

    fun accept(character: Character) = viewModelScope.launch {
        _char.emit(character)
    }

    fun changeEvoPhase(phase: Int) = viewModelScope.launch {
        var skillIndex = -1
        val char = _char.value
        val skills = char.skills.onEachIndexed { index, skill ->
            if (phase >= index) {
                skill.unlock = 1
                skillIndex = index
            } else {
                skill.unlock = 0
            }
        }.toList()
        var currentEquip = char.currentEquip
        val equip = char.equip.onEach { (equipId, data) ->
            if (phase >= 2) {
                data.unlock()
                currentEquip = equipId
            } else {
                data.lock()
                currentEquip = null
            }
        }.toMutableMap()
        val maxLevel = Table.getMaxCharLevel(char.charId, phase)
        val maxSkillLevel = if (phase < 1) 4 else 7
        _maxLevel.emit(maxLevel)
        _maxSkillLevel.emit(maxSkillLevel)
        _char.emit(
            char.copy(
                evolvePhase = phase,
                mainSkillLvl = min(maxSkillLevel, char.mainSkillLvl),
                skills = skills,
                defaultSkillIndex = skillIndex,
                currentEquip = currentEquip,
                equip = equip,
                level = min(maxLevel, char.level)
            )
        )
    }

    fun updateMaxEvoPhase(charId: String) = viewModelScope.launch {
        _maxEvoPhase.emit(Table.getMaxCharEvoLevel(charId))
    }

    fun updateMaxLevel(charId: String, phase: Int) = viewModelScope.launch {
        _maxLevel.emit(Table.getMaxCharLevel(charId, phase))
    }

    fun updateMaxSkillLevel(phase: Int) = viewModelScope.launch {
        _maxSkillLevel.emit(if (phase < 1) 4 else 7)
    }

}