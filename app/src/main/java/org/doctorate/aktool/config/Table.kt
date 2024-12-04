package org.doctorate.aktool.config

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.doctorate.aktool.R
import org.doctorate.aktool.utils.JsonUtil
import org.doctorate.aktool.utils.ListMap
import org.doctorate.aktool.utils.NestingMap
import org.doctorate.aktool.utils.get
import java.io.File
import java.net.URI

object Table {
    private val jsonList = listOf("character_table.json", "favor_table.json")
    var CHARACTER_TABLE: NestingMap = mapOf()
        private set
    var FAVOR_TABLE: Map<String, Any> = mapOf()
        private set

    fun initData(context: Context): Boolean {
        val dir = context.filesDir
        val fileList = jsonList.map {
            File(dir, "data/excel/$it").also {
                if (it.exists().not()) {
                    Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        CHARACTER_TABLE = JsonUtil.fromJson(fileList[0])
        FAVOR_TABLE = JsonUtil.fromJson(fileList[2])
        return true
    }

    suspend fun refreshData(context: Context, uri: String) = withContext(Dispatchers.IO) {
        val dir = context.filesDir
        jsonList.forEachIndexed { index, fileName ->
            val file = File(dir, "data/excel/$fileName")
            val uri = URI("$uri/assetbundle/excel/$fileName").toURL()
            when (index) {
                0 -> CHARACTER_TABLE = JsonUtil.fromJson<NestingMap>(uri).write(file)
                1 -> FAVOR_TABLE = JsonUtil.fromJson<Map<String, Any>>(uri).write(file)
            }
        }
    }

    private fun <K, V> Map<K, V>.write(file: File): Map<K, V> =
        also { JsonUtil.writeToFile(this, file) }

    fun getCharacterData(charId: String): Map<String, Any> {
        return CHARACTER_TABLE[charId] ?: throw RuntimeException("$charId is not exists")
    }

    fun getMaxCharEvoLevel(charId: String): Int {
        return getCharacterData(charId).get<ListMap>("phases")!!.size - 1
    }

    fun getMaxCharLevel(charId: String, evoPhase: Int): Int {
        val phases = getCharacterData(charId).get<ListMap>("phases")!!
        val phase = phases.getOrNull(evoPhase) ?: phases.first()
        return phase["maxLevel"] as Int
    }

    fun getRealFavPoint(percent: Int): Int {
        val favorFrames = FAVOR_TABLE.get<ListMap>("favorFrames")!!
        return favorFrames[percent]["level"] as Int
    }

    fun getFavPointPercent(favPoint: Int): Int {
        val favorFrames = FAVOR_TABLE.get<ListMap>("favorFrames")!!
        favorFrames.forEach {
            if (it["level"] as Int >= favPoint) {
                return it.get<Map<String, Int>>("data")!!["percent"]!!
            }
        }
        return 0
    }

}