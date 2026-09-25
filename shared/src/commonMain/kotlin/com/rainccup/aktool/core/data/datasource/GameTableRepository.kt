package com.rainccup.aktool.core.data.datasource

import com.rainccup.aktool.core.common.decodeToMap
import com.rainccup.aktool.core.network.ApiClient
import com.rainccup.aktool.core.platform.AppPaths

class GameTableRepository(
    private val paths: AppPaths,
    private val api: ApiClient,
) {
    private val jsonFiles =
        listOf("character_table.json", "favor_table.json", "uniequip_table.json")

    var characterTable: Map<String, Map<String, Any?>> = emptyMap()
        private set
    private var favorTable: Map<String, Any?> = emptyMap()
    private var uniequipTable: Map<String, Any?> = emptyMap()

    fun init(): Boolean {
        if (characterTable.isNotEmpty() && favorTable.isNotEmpty()) return true
        val missing = jsonFiles.any { !fileExists("${paths.dataDir}/excel/$it") }
        if (missing) return false
        characterTable = castMap(decodeToMap(readFileText("${paths.dataDir}/excel/${jsonFiles[0]}")!!))
        favorTable = decodeToMap(readFileText("${paths.dataDir}/excel/${jsonFiles[1]}")!!)
        uniequipTable = decodeToMap(readFileText("${paths.dataDir}/excel/${jsonFiles[2]}")!!)
        return true
    }

    suspend fun refresh(uri: String) {
        jsonFiles.forEachIndexed { index, fileName ->
            val text = api.downloadText("${uri.trimEnd('/')}/assetbundle/excel/$fileName")
            writeFileText("${paths.dataDir}/excel/$fileName", text)
            when (index) {
                0 -> characterTable = castMap(decodeToMap(text))
                1 -> favorTable = decodeToMap(text)
                2 -> uniequipTable = decodeToMap(text)
            }
        }
    }

    fun getCharacterData(charId: String): Map<String, Any?> =
        characterTable[charId] ?: error("$charId is not exists")

    fun getMaxCharEvoLevel(charId: String): Int {
        val phases = getCharacterData(charId)["phases"] as? List<*> ?: return 0
        return phases.size - 1
    }

    fun getMaxCharLevel(charId: String, evoPhase: Int): Int {
        val phases = getCharacterData(charId)["phases"] as? List<*> ?: return 0
        val phase = (phases.getOrNull(evoPhase) ?: phases.first()) as Map<*, *>
        return (phase["maxLevel"] as Number).toInt()
    }

    fun getRealFavPoint(percent: Int): Int {
        val frames = favorTable["favorFrames"] as? List<*> ?: return 0
        val data = (frames.getOrNull(percent) ?: frames.last()) as Map<*, *>
        return (data["level"] as Number).toInt()
    }

    fun getFavPointPercent(favPoint: Int): Int {
        val frames = favorTable["favorFrames"] as? List<*> ?: return 0
        frames.forEach { raw ->
            val row = raw as Map<*, *>
            if ((row["level"] as Number).toInt() >= favPoint) {
                val data = row["data"] as Map<*, *>
                return (data["percent"] as Number).toInt()
            }
        }
        return 0
    }

    fun getEquipType(equipId: String): String {
        val dict = uniequipTable["equipDict"] as? Map<*, *> ?: return "original"
        val equip = dict[equipId] as? Map<*, *> ?: return "original"
        return equip["typeIcon"] as? String ?: "original"
    }

    internal fun seedForTest(character: String, favor: String, uniequip: String) {
        characterTable = castMap(decodeToMap(character))
        favorTable = decodeToMap(favor)
        uniequipTable = decodeToMap(uniequip)
    }

    @Suppress("UNCHECKED_CAST")
    private fun castMap(map: Map<String, Any?>): Map<String, Map<String, Any?>> =
        map as Map<String, Map<String, Any?>>
}
