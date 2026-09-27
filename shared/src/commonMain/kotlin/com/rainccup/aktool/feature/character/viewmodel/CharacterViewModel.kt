package com.rainccup.aktool.feature.character.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.common.replace
import com.rainccup.aktool.core.domain.model.CharacterFilter
import com.rainccup.aktool.core.domain.usecase.character.GainCharacterUseCase
import com.rainccup.aktool.core.domain.usecase.character.LoadCharactersUseCase
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.game_table_init_fail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.milliseconds

class CharacterViewModel(
    private val loadCharacters: LoadCharactersUseCase,
    private val gainCharacter: GainCharacterUseCase,
    private val messenger: Messenger,
) : ViewModel() {
    val characterData = mutableMapOf<String, Character>()
    private val characterList = mutableStateListOf<Character>()
    private val charNameMap = mutableMapOf<String, String>()

    private val _splash = MutableStateFlow(true)
    val splash: StateFlow<Boolean> = _splash.asStateFlow()

    private val _isSelect = MutableStateFlow(false)
    val isSelect: StateFlow<Boolean> = _isSelect.asStateFlow()

    private val _profession = MutableStateFlow("ALL")
    val profession: StateFlow<String> = _profession.asStateFlow()

    private val _loadAnimate = MutableStateFlow(false)
    val loadAnimate: StateFlow<Boolean> = _loadAnimate.asStateFlow()

    private val _gainChar = MutableStateFlow(false)
    val gainChar: StateFlow<Boolean> = _gainChar.asStateFlow()

    private val _isSearch = MutableStateFlow(false)
    val isSearch: StateFlow<Boolean> = _isSearch.asStateFlow()

    fun charList(): List<Character> = characterList.apply { sort() }

    fun initCharData() = viewModelScope.launch {
        if (!loadAnimate.value) {
            if (!loadCharacters.init()) {
                messenger.show(getString(Res.string.game_table_init_fail))
                return@launch
            }
            _loadAnimate.emit(true)
            characterData.clear()
            runCatching {
                val loaded = loadCharacters()
                characterData.putAll(loaded.characters)
                charNameMap.clear()
                charNameMap.putAll(loaded.nameToId)
                selectProfession(_profession.value)
                delay(500.milliseconds)
            }.onFailure {
                messenger.show(it.message ?: "error")
            }
            closeAnimate()
        }
    }

    fun existChar(charId: String): Boolean = loadCharacters.existChar(charId)

    fun selectProfession(profession: String) = viewModelScope.launch {
        _profession.emit(profession)
        characterList.clear()
        characterList.addAll(CharacterFilter.byProfession(characterData.values, profession))
    }

    fun changeSelectState(state: Boolean) = viewModelScope.launch {
        _isSelect.emit(state)
    }

    fun updateCharData(char: Character) = viewModelScope.launch {
        characterData[char.instId.toString()] = char
        selectProfession(_profession.value)
    }

    fun closeAnimate() = viewModelScope.launch {
        _splash.emit(false)
        _loadAnimate.emit(false)
    }

    fun gainChar(charId: String) = viewModelScope.launch {
        gainCharacter(charId)
    }

    fun changeGainCharState() = viewModelScope.launch {
        _gainChar.emit(_gainChar.value.not())
    }

    fun getSearchedCharList(charName: String): List<String> =
        CharacterFilter.matches(charNameMap, charName)

    fun changeSearchState() = viewModelScope.launch {
        _isSearch.emit(_isSearch.value.not())
    }

    fun getCharIdByCharName(charName: String): String =
        CharacterFilter.idOf(charNameMap, charName)

    fun searchChar(charName: String) {
        characterData.values.find { it.name == charName }?.let {
            characterList.replace(it)
        }
    }
}
