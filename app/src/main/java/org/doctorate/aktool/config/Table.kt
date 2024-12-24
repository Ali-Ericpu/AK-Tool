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
    private val jsonList = listOf("character_table.json", "favor_table.json", "uniequip_table.json")
    var CHARACTER_TABLE: NestingMap = mapOf()
        private set
    private var FAVOR_TABLE: Map<String, Any> = mapOf()
    private var UNI_EQUIP_TABLE: Map<String, Any> = mapOf()

    fun initData(context: Context): Boolean {
        if (CHARACTER_TABLE.isNotEmpty() && FAVOR_TABLE.isNotEmpty()) {
            return true
        }
        val dir = context.filesDir
        val fileList = jsonList.map { name ->
            File(dir, "data/excel/$name").also {
                if (it.exists().not()) {
                    Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        CHARACTER_TABLE = JsonUtil.fromJson(fileList[0])
        FAVOR_TABLE = JsonUtil.fromJson(fileList[1])
        UNI_EQUIP_TABLE = JsonUtil.fromJson(fileList[2])
        return true
    }

    suspend fun refreshData(context: Context, uri: String) = withContext(Dispatchers.IO) {
        val dir = context.filesDir
        jsonList.forEachIndexed { index, fileName ->
            val file = File(dir, "data/excel/$fileName")
            val url = URI("$uri/assetbundle/excel/$fileName").toURL()
            when (index) {
                0 -> CHARACTER_TABLE = JsonUtil.fromJson<NestingMap>(url).write(file)
                1 -> FAVOR_TABLE = JsonUtil.fromJson<Map<String, Any>>(url).write(file)
                2 -> UNI_EQUIP_TABLE = JsonUtil.fromJson<Map<String, Any>>(url).write(file)
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
        val data = favorFrames.getOrNull(percent) ?: favorFrames.last()
        return data["level"] as Int
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

    fun getEquipType(equipId:String) :String {
        return UNI_EQUIP_TABLE.get<NestingMap>("equipDict")!![equipId]?.get<String>("typeIcon") ?: "original"
    }

}