package com.rainccup.aktool.core.model

import com.rainccup.aktool.core.common.JsonUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelSerializationTest {

    @Test
    fun resultEnvelope_roundTrip() {
        val raw =
            """{"msg":"ok","status":0,"type":"OK","data":{"id":"x","type":"CHAR","count":1}}"""
        val decoded = JsonUtil.decode<ApiResult<Item>>(raw)
        assertEquals(0, decoded.status)
        assertEquals("x", decoded.data?.id)
        val re = JsonUtil.decode<ApiResult<Item>>(JsonUtil.encode(decoded))
        assertEquals(decoded, re)
    }

    @Test
    fun saveStatusRequest_omitsNulls() {
        val req = SaveStatusRequest(level = 10)
        val text = JsonUtil.encode(req)
        assertTrue("\"level\":10" in text)
        assertFalse("nickName" in text)
    }

    @Test
    fun appConfig_defaults() {
        val cfg = JsonUtil.decode<AppConfig>("""{"serverUri":"http://127.0.0.1"}""")
        assertEquals("http://127.0.0.1", cfg.serverUri)
        assertEquals(false, cfg.darkMode)
        assertEquals("", cfg.bgPath)
    }

    @Test
    fun appConfig_primaryColorSurvivesTheConfigFileRoundTrip() {
        // 主题色存的是 Color.value（打包后的原始 ULong，超出 Long 范围），
        // 必须确认经过 ConfigRepository 的 pretty JSON 往返后依然无损
        val picked = AppConfig(primaryColor = 0xFF1E88E500000000UL)
        assertEquals(picked, JsonUtil.decode<AppConfig>(JsonUtil.encodePretty(picked)))
        // 未设置时的哨兵值同样要能原样读回
        assertEquals(AppConfig(), JsonUtil.decode<AppConfig>(JsonUtil.encodePretty(AppConfig())))
    }

    @Test
    fun saveStatusRequest_carriesBirthdayAndStillOmitsUnsetOnes() {
        val withBirthday = JsonUtil.encode(SaveStatusRequest(birthday = Birthday(3, 21)))
        assertTrue("\"birthday\":{\"month\":3,\"day\":21}" in withBirthday)
        // 没设置生日时不应出现该字段：null 被 explicitNulls = false 省略
        assertFalse("birthday" in JsonUtil.encode(SaveStatusRequest(level = 10)))
    }

    @Test
    fun status_acceptsMissingBirthdayAndRoundTripsIt() {
        val raw = """
            {"nickName":"n","nickNumber":"1145","level":1,"exp":0,"socialPoint":0,
             "gachaTicket":0,"tenGachaTicket":0,"instantFinishTicket":0,"hggShard":0,
             "lggShard":0,"recruitLicense":0,"progress":0,"buyApRemainTimes":0,
             "apLimitUpFlag":0,"uid":"1","ap":0,"maxAp":0,"androidDiamond":0,
             "iosDiamond":0,"diamondShard":0,"gold":0,"practiceTicket":0,
             "lastRefreshTs":0,"lastApAddTime":0,"registerTs":0,"lastOnlineTs":0,
             "serverName":"TERRA","avatarId":"","resume":"","friendNumLimit":0,
             "monthlySubscriptionStartTime":0,"monthlySubscriptionEndTime":0,
             "secretary":"","secretarySkinId":"","tipMonthlyCardExpireTs":0,
             "classicShard":0,"classicGachaTicket":0,"classicTenGachaTicket":0}
        """.trimIndent()
        // 旧服务器不带 birthday 字段也必须能解码
        assertEquals(null, JsonUtil.decode<Status>(raw).birthday)
        val withoutTail = raw.removeSuffix("}")
        val withBirthday = JsonUtil.decode<Status>("$withoutTail,\"birthday\":{\"month\":3,\"day\":21}}")
        assertEquals(Birthday(3, 21), withBirthday.birthday)
        assertEquals(withBirthday, JsonUtil.decode<Status>(JsonUtil.encode(withBirthday)))
    }

    @Test
    fun character_minimumFields() {
        val raw = """
            {"instId":1,"charId":"char_002_amiya","favorPoint":0,"potentialRank":0,
             "mainSkillLvl":1,"skin":"char_002_amiya#1","level":1,"exp":0,"evolvePhase":0,
             "defaultSkillIndex":0,"gainTime":0,"skills":[],"voiceLan":"CN",
             "equip":{},"starMark":0}
        """.trimIndent()
        val char = JsonUtil.decode<Character>(raw)
        assertEquals("char_002_amiya", char.charId)
        assertEquals(null, char.name)
    }
}
