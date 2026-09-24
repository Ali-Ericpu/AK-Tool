package com.rainccup.aktool.ui.character

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rainccup.aktool.core.datastore.GameTableRepository
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.GainItemRequest
import com.rainccup.aktool.core.model.Item
import com.rainccup.aktool.core.model.SaveCharRequest
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.core.repository.AdminRepository
import com.rainccup.aktool.utils.replace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class CharacterViewModel(
    private val admin: AdminRepository,
    private val gameTable: GameTableRepository,
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
            _loadAnimate.emit(true)
            characterData.clear()
            runCatching {
                gameTable.characterTable.forEach { (charId, charData) ->
                    if (charId.startsWith("char_") && charData["displayNumber"] != null) {
                        charNameMap[charData["name"] as String] = charId
                    }
                }
                val result = admin.syncCharacter()
                if (result.data == null) {
                    throw RuntimeException("数据异常")
                }
                result.data.forEach { (instId, char) ->
                    runCatching { gameTable.getCharacterData(char.charId) }.onSuccess {
                        val name = it["name"] as String
                        val profession = it["profession"] as String
                        val rarity = (it["rarity"] as String).substringAfter("_").toInt()
                        characterData[instId] = char.copy(
                            name = name,
                            profession = profession,
                            rarity = rarity
                        )
                    }.onFailure { Logger.d { "Character_Init_CharData ${it.message}" } }
                }
                selectProfession(_profession.value)
                delay(500.milliseconds)
            }.onFailure {
                messenger.show(it.message ?: "error")
            }
            closeAnimate()
        }
    }

    fun selectProfession(profession: String) = viewModelScope.launch {
        _profession.emit(profession)
        characterList.clear()
        if (profession == "ALL") {
            characterList.addAll(characterData.values)
        } else {
            characterList.addAll(characterData.values.filter { it.profession == profession })
        }
    }

    fun changeSelectState(state: Boolean) = viewModelScope.launch {
        _isSelect.emit(state)
    }

    suspend fun changeCharData(char: Character) = withContext(Dispatchers.Default) {
        val result = admin.saveCharacter(SaveCharRequest(char.instId, char))
        if (result.status != 0) {
            throw RuntimeException(result.msg)
        }
        characterData[char.instId.toString()] = char
        selectProfession(_profession.value)
    }

    fun closeAnimate() = viewModelScope.launch {
        _splash.emit(false)
        _loadAnimate.emit(false)
    }

    suspend fun gainChar(charId: String) = withContext(Dispatchers.Default) {
        val request = GainItemRequest(listOf(Item(charId, "CHAR", 1)))
        val result = admin.gainItem(request)
        if (result.status != 0) {
            throw RuntimeException(result.msg)
        }
    }

    fun changeGainCharState() = viewModelScope.launch {
        _gainChar.emit(_gainChar.value.not())
    }

    fun getSearchedCharList(charName: String): List<String> {
        if (charName.isEmpty()) return emptyList()
        val found = mutableListOf<String>()
        for (name in charNameMap.keys) {
            if (charName in name) {
                found.add(name)
            }
            if (found.size == 10) break
        }
        return found
    }

    fun changeSearchState() = viewModelScope.launch {
        _isSearch.emit(_isSearch.value.not())
    }

    fun getCharIdByCharName(charName: String): String = charNameMap[charName] ?: "ERROR"

    fun searchChar(charName: String) {
        characterData.values.find { it.name == charName }?.let {
            characterList.replace(it)
        }
    }
}
