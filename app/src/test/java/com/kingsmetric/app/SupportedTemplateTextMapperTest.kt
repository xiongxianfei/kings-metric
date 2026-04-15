package com.kingsmetric.app

import com.kingsmetric.importflow.Anchor
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.Section
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedTemplateTextMapperTest {

    @Test
    fun supported_mapper_helper_exception_does_not_abort_analysis() {
        val analysis = SupportedTemplateTextMapper.map(
            text = """
                胜利
                20 vs 10
                数据 复盘
                对英雄輸出: 171.2k 1st
                輸出伤害
                35.3%
                承受英雄伤害: 82.1k
                承伤
                20.3%
                经济 13.1k
                经济占比 24%
                参团率 80.0%
            """.trimIndent(),
            requestedFields = FieldKey.all,
            playerSummaryLineExtractor = { error("unexpected summary parse failure") }
        )

        assertEquals("胜利", analysis.rawValues[FieldKey.RESULT])
        assertEquals("20 vs 10", analysis.rawValues[FieldKey.SCORE])
        assertEquals("171.2k", analysis.rawValues[FieldKey.DAMAGE_DEALT])
        assertEquals("35.3%", analysis.rawValues[FieldKey.DAMAGE_SHARE])
        assertEquals("82.1k", analysis.rawValues[FieldKey.DAMAGE_TAKEN])
        assertEquals("20.3%", analysis.rawValues[FieldKey.DAMAGE_TAKEN_SHARE])
        assertEquals("24%", analysis.rawValues[FieldKey.GOLD_SHARE])
        assertEquals("80.0%", analysis.rawValues[FieldKey.PARTICIPATION_RATE])
        assertTrue(Anchor.RESULT_HEADER in analysis.anchors)
        assertTrue(Anchor.DATA_TAB_SELECTED in analysis.anchors)
        assertEquals(
            setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION),
            analysis.visibleSections
        )
    }

    @Test
    fun supported_readable_diagnostics_ocr_dump_maps_to_supported_analysis() {
        val analysis = SupportedTemplateTextMapper.map(
            text = """
                |未插卡D令
                胜利
                总览
                15
                20 vs 10 巅峰赛
                输出
                不败、菜鸟 13.1 1111/5
                我方胜利
                关键团战输出
                |个人数据
                数据 复盘
                不败、菜鸟
                輸出伤害
                35.3%
                峰赛胜利
                t:
                承伤 总承受伤害
                |经济:
                对英雄輸出: 171.2k 1st
                承受英雄伤害:82.1k
                经济 打野经济:
                |参团率:
                团队 控制时长:
                611.1k
                承受伤害
                20.3%
                96.3k
                13.1k
                1.4k
                同队对比
                ®金牌发有路)
                3s
                3rd
                2nd
                80.0%2nd
                |总经济
                生成精彩时刻
                N81%
                输出转化率:1.5
                每死承伤:
                2nd 补刀数:
                输出占比: 35.3%
                经济占比
                治疗量:
                承伤占比: 20.3%
                对塔伤害:
                24.0%
                MVP
                对位对比
                24%
                50
                82.1k 2nd
                3.8k
                10.9k
                1st
                J 19:53
                参团率
                1st
                2nd
                11/1/5
                80.0%
            """.trimIndent(),
            requestedFields = FieldKey.all
        )

        assertEquals("胜利", analysis.rawValues[FieldKey.RESULT])
        assertEquals("20 vs 10", analysis.rawValues[FieldKey.SCORE])
        assertEquals("11/1/5", analysis.rawValues[FieldKey.KDA])
        assertEquals("不败、菜鸟", analysis.rawValues[FieldKey.PLAYER_NAME])
        assertEquals("13.1", analysis.rawValues[FieldKey.TOTAL_GOLD])
        assertEquals("171.2k", analysis.rawValues[FieldKey.DAMAGE_DEALT])
        assertEquals("35.3%", analysis.rawValues[FieldKey.DAMAGE_SHARE])
        assertEquals("82.1k", analysis.rawValues[FieldKey.DAMAGE_TAKEN])
        assertEquals("20.3%", analysis.rawValues[FieldKey.DAMAGE_TAKEN_SHARE])
        assertEquals("24%", analysis.rawValues[FieldKey.GOLD_SHARE])
        assertEquals("1.4k", analysis.rawValues[FieldKey.GOLD_FROM_FARMING])
        assertEquals("50", analysis.rawValues[FieldKey.LAST_HITS])
        assertEquals("80.0%", analysis.rawValues[FieldKey.PARTICIPATION_RATE])
        assertEquals("3s", analysis.rawValues[FieldKey.CONTROL_DURATION])
        assertEquals("10.9k", analysis.rawValues[FieldKey.DAMAGE_DEALT_TO_OPPONENTS])
        assertTrue(Anchor.RESULT_HEADER in analysis.anchors)
        assertTrue(Anchor.DATA_TAB_SELECTED in analysis.anchors)
        assertTrue(Anchor.SUMMARY_CARD in analysis.anchors)
        assertEquals(
            setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION),
            analysis.visibleSections
        )
    }

    @Test
    fun supported_real_device_ocr_dump_maps_to_supported_analysis() {
        val analysis = SupportedTemplateTextMapper.map(
            text = """
                |未插卡D令
                胜利
                总览
                15
                20 vs 10 巅峰赛
                输出
                不败、菜鸟 13.1 1111/5
                我方胜利
                关键团战输出
                |个人数据
                数据 复盘
                不败、菜鸟
                輸出伤害
                35.3%
                峰赛胜利
                t:
                承伤 总承受伤害
                |经济:
                对英雄輸出: 171.2k 1st
                承受英雄伤害:82.1k
                经济 打野经济:
                |参团率:
                团队 控制时长:
                611.1k
                承受伤害
                20.3%
                96.3k
                13.1k
                1.4k
                同队对比
                ®金牌发有路)
                3s
                3rd
                2nd
                80.0%2nd
                |总经济
                生成精彩时刻
                N81%
                输出转化率:1.5
                每死承伤:
                2nd 补刀数:
                输出占比: 35.3%
                经济占比
                治疗量:
                承伤占比: 20.3%
                对塔伤害:
                24.0%
                MVP
                对位对比
                24%
                50
                82.1k 2nd
                3.8k
                10.9k
                1st
                J 19:53
                参团率
                1st
                2nd
                11/1/5
                80.0%
            """.trimIndent(),
            requestedFields = FieldKey.all
        )

        assertEquals("胜利", analysis.rawValues[FieldKey.RESULT])
        assertEquals("20 vs 10", analysis.rawValues[FieldKey.SCORE])
        assertEquals("11/1/5", analysis.rawValues[FieldKey.KDA])
        assertEquals("不败、菜鸟", analysis.rawValues[FieldKey.PLAYER_NAME])
        assertEquals("13.1", analysis.rawValues[FieldKey.TOTAL_GOLD])
        assertEquals("171.2k", analysis.rawValues[FieldKey.DAMAGE_DEALT])
        assertEquals("35.3%", analysis.rawValues[FieldKey.DAMAGE_SHARE])
        assertEquals("82.1k", analysis.rawValues[FieldKey.DAMAGE_TAKEN])
        assertEquals("20.3%", analysis.rawValues[FieldKey.DAMAGE_TAKEN_SHARE])
        assertTrue(analysis.rawValues[FieldKey.GOLD_SHARE]!!.startsWith("24"))
        assertEquals("1.4k", analysis.rawValues[FieldKey.GOLD_FROM_FARMING])
        assertEquals("50", analysis.rawValues[FieldKey.LAST_HITS])
        assertEquals("80.0%", analysis.rawValues[FieldKey.PARTICIPATION_RATE])
        assertEquals("3s", analysis.rawValues[FieldKey.CONTROL_DURATION])
        assertEquals("10.9k", analysis.rawValues[FieldKey.DAMAGE_DEALT_TO_OPPONENTS])
        assertTrue(Anchor.RESULT_HEADER in analysis.anchors)
        assertTrue(Anchor.DATA_TAB_SELECTED in analysis.anchors)
        assertTrue(Anchor.SUMMARY_CARD in analysis.anchors)
        assertEquals(
            setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION),
            analysis.visibleSections
        )
    }
}
