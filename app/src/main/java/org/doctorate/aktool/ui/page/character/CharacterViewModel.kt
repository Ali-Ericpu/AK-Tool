package org.doctorate.aktool.ui.page.character

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.doctorate.aktool.config.Table
import org.doctorate.aktool.network.Network
import org.doctorate.aktool.pojo.entity.Character
import org.doctorate.aktool.pojo.entity.Item
import org.doctorate.aktool.pojo.request.GainItemRequest
import org.doctorate.aktool.pojo.request.SaveCharRequest
import org.doctorate.aktool.utils.replace

class CharacterViewModel : ViewModel() {
    val characterData = mutableMapOf<String, Character>()
    private val characterList = mutableStateListOf<Character>()
    private val charNameMap = mutableMapOf<String, String>()

    private var _splash = MutableStateFlow(true)
    val splash = _splash.asStateFlow()

    private var _isSelect = MutableStateFlow(false)
    val isSelect = _isSelect.asStateFlow()

    private var _profession = MutableStateFlow("ALL")
    val profession = _profession.asStateFlow()

    private var _loadAnimate = MutableStateFlow(false)
    val loadAnimate = _loadAnimate.asStateFlow()

    private var _gainChar = MutableStateFlow(false)
    val gainChar = _gainChar.asStateFlow()

    private var _isSearch = MutableStateFlow(false)
    val isSearch = _isSearch.asStateFlow()

    fun charList(): List<Character> = characterList.apply { sort() }

    fun initCharData(context: Context) = viewModelScope.launch {
        if (!loadAnimate.value) {
            _loadAnimate.emit(true)
            characterData.clear()
            runCatching {
                launch {
                    charNameMap.clear()
                    Table.CHARACTER_TABLE.forEach { (charId, charData) ->
                        if (charId.startsWith("char_") && charData["displayNumber"] != null) {
                            charNameMap[charData["name"] as String] = charId
                        }
                    }
                }
                val result = Network.syncCharacter()
                if (result.data == null) {
                    throw RuntimeException("数据异常")
                }
                result.data.forEach { (instId, char) ->
                    runCatching { Table.getCharacterData(char.charId) }.onSuccess {
                        char.name = it["name"] as String
                        char.profession = it["profession"] as String
                        char.rank = (it["rarity"] as String).substringAfter("_").toInt()
                        characterData[instId] = char
                    }.onFailure { Log.d("Character_Init_CharData", it.message.toString()) }
                }
                selectProfession(_profession.value)
                delay(500)
            }.onFailure {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            }
            closeAnimate()
        }
    }

    fun selectProfession(profession: String) = viewModelScope.launch {
        Log.d("Character_Select_Profession", "selectProfession: $profession")
        _profession.emit(profession)
        characterList.clear()
        if (profession == "ALL") {
            characterList.addAll(characterData.values)
        } else {
            characterList.addAll(characterData.values.filter { it.profession!! == profession })
        }
    }

    fun changeSelectState(state: Boolean) = viewModelScope.launch {
        _isSelect.emit(state)
    }

    suspend fun changeCharData(char: Character) = withContext(Dispatchers.IO) {
        val result = Network.saveCharacter(SaveCharRequest(char.instId, char))
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

    suspend fun gainChar(charId: String) = withContext(Dispatchers.IO) {
        val request = GainItemRequest(listOf(Item(charId, "CHAR", 1)))
        val result = Network.gainItem(request)
        if (result.status != 0) {
            throw RuntimeException(result.msg)
        }
    }

    fun changeGainCharState() = viewModelScope.launch {
        _gainChar.emit(_gainChar.value.not())
    }

    fun getSearchedCharList(charName: String): List<String> {
        if (charName.isEmpty()) return emptyList()
        val charList = mutableListOf<String>()
        for (name in charNameMap.keys) {
            if (charName in name) {
                charList.add(name)
            }
            if (charList.size == 10) {
                break
            }
        }
        return charList
    }

    fun changeSearchState() = viewModelScope.launch {
        _isSearch.emit(_isSearch.value.not())
    }

    fun getCharIdByCharName(charName: String): String {
        return charNameMap[charName] ?: "ERROR"
    }

    fun searchChar(charName: String) {
        characterData.values.find { it.name == charName }?.let {
            characterList.replace(it)
        }
    }

}
