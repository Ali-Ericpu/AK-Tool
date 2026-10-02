package com.rainccup.aktool.core.data.datasource

import com.rainccup.aktool.core.common.ListMap
import com.rainccup.aktool.core.common.NestingMap
import com.rainccup.aktool.core.common.decodeToMap
import com.rainccup.aktool.core.common.getTyped
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.platform.AppPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameTableRepository(
    private val paths: AppPaths,
    private val api: ApiClient,
) {
    private val jsonFiles =
        listOf("character_table.json", "favor_table.json", "uniequip_table.json")

    var characterTable: NestingMap<Any?> = emptyMap()
        private set
    private var favorTable: Map<String, Any?> = emptyMap()
    private var uniequipTable: Map<String, Any?> = emptyMap()

    suspend fun init(): Boolean {
        if (characterTable.isNotEmpty() && favorTable.isNotEmpty()) return true
        val missing = jsonFiles.any { !fileExists("${paths.dataDir}/excel/$it") }
        return !missing && withContext(Dispatchers.IO) {
            characterTable = decodeToMap(readFileText("${paths.dataDir}/excel/${jsonFiles[0]}")!!)
            favorTable = decodeToMap(readFileText("${paths.dataDir}/excel/${jsonFiles[1]}")!!)
            uniequipTable = decodeToMap(readFileText("${paths.dataDir}/excel/${jsonFiles[2]}")!!)
            true
        }
    }

    suspend fun refresh(uri: String) = withContext(Dispatchers.IO) {
        jsonFiles.forEachIndexed { index, fileName ->
            val text = api.downloadText("${uri.trimEnd('/')}/assetbundle/excel/$fileName")
            writeFileText("${paths.dataDir}/excel/$fileName", text)
            when (index) {
                0 -> characterTable = decodeToMap(text)
                1 -> favorTable = decodeToMap(text)
                2 -> uniequipTable = decodeToMap(text)
            }
        }
    }

    fun getCharacterData(charId: String): Map<String, Any?> =
        characterTable[charId] ?: error("$charId is not exists")

    fun getMaxCharEvoLevel(charId: String): Int {
        val phases = getCharacterData(charId).getTyped<ListMap<Any>>("phases") ?: return 0
        return phases.size - 1
    }

    fun getMaxCharLevel(charId: String, evoPhase: Int): Int {
        val phases = getCharacterData(charId).getTyped<ListMap<Any>>("phases") ?: return 0
        val phase = phases.getOrNull(evoPhase) ?: phases.first()
        return phase.getTyped<Number>("maxLevel")!!.toInt()
    }

    fun getRealFavPoint(percent: Int): Int {
        val frames = favorTable.getTyped<ListMap<Any>>("favorFrames") ?: return 0
        val data = frames.getOrNull(percent) ?: frames.last()
        return data.getTyped<Number>("level")?.toInt() ?: 0
    }

    fun getFavPointPercent(favPoint: Int): Int {
        val frames = favorTable.getTyped<ListMap<Any>>("favorFrames") ?: return 0
        frames.forEach { frame ->
            if (frame.getTyped<Number>("level")!!.toInt() >= favPoint) {
                val data = frame.getTyped<Map<String, Any>>("data")!!
                return data.getTyped<Number>("percent")!!.toInt()
            }
        }
        return 0
    }

    fun getEquipType(equipId: String): String {
        val dict = uniequipTable.getTyped<NestingMap<Any>>("equipDict") ?: return "original"
        val equip = dict[equipId] ?: return "original"
        return equip.getTyped("typeIcon") ?: "original"
    }

    internal fun seedForTest(character: String, favor: String, uniequip: String) {
        characterTable = decodeToMap(character)
        favorTable = decodeToMap(favor)
        uniequipTable = decodeToMap(uniequip)
    }

}
