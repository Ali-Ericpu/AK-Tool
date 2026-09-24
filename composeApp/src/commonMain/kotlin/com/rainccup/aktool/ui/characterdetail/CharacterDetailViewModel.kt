package com.rainccup.aktool.ui.characterdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.datastore.GameTableRepository
import com.rainccup.aktool.core.model.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class CharacterDetailViewModel(
    private val gameTable: GameTableRepository,
    val charInstId: String,
) : ViewModel() {
    private val _char = MutableStateFlow(Character.placeholder())
    val char: StateFlow<Character> = _char.asStateFlow()

    private val _maxEvoPhase = MutableStateFlow(2)
    val maxEvoPhase: StateFlow<Int> = _maxEvoPhase.asStateFlow()

    private val _maxLevel = MutableStateFlow(90)
    val maxLevel: StateFlow<Int> = _maxLevel.asStateFlow()

    private val _maxSkillLevel = MutableStateFlow(7)
    val maxSkillLevel: StateFlow<Int> = _maxSkillLevel.asStateFlow()

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
        val maxLevel = gameTable.getMaxCharLevel(char.charId, phase)
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
                level = min(maxLevel, char.level),
            )
        )
    }

    fun updateMaxEvoPhase(charId: String) = viewModelScope.launch {
        _maxEvoPhase.emit(gameTable.getMaxCharEvoLevel(charId))
    }

    fun updateMaxLevel(charId: String, phase: Int) = viewModelScope.launch {
        _maxLevel.emit(gameTable.getMaxCharLevel(charId, phase))
    }

    fun updateMaxSkillLevel(phase: Int) = viewModelScope.launch {
        _maxSkillLevel.emit(if (phase < 1) 4 else 7)
    }
}
