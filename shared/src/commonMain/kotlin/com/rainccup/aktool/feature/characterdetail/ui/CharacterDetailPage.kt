package com.rainccup.aktool.feature.characterdetail.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.rainccup.aktool.core.designsystem.component.IntRangeSlider
import com.rainccup.aktool.core.designsystem.component.PageActionRow
import com.rainccup.aktool.core.designsystem.component.roundedBackground
import com.rainccup.aktool.core.domain.GameTableQuery
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.Equip
import com.rainccup.aktool.core.navigation.characterCardSharedElement
import com.rainccup.aktool.core.platform.platformUiScale
import com.rainccup.aktool.feature.character.ui.CharacterCard
import com.rainccup.aktool.feature.character.ui.equipPainter
import com.rainccup.aktool.feature.character.ui.skillPainter
import com.rainccup.aktool.feature.characterdetail.viewmodel.CharacterDetailViewModel
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.character_locked_skill
import com.rainccup.aktool.resources.character_skill_selected
import com.rainccup.aktool.resources.character_special_skill_0
import com.rainccup.aktool.resources.character_special_skill_1
import com.rainccup.aktool.resources.character_special_skill_2
import com.rainccup.aktool.resources.character_special_skill_3
import com.rainccup.aktool.resources.equip
import com.rainccup.aktool.resources.evp_phase
import com.rainccup.aktool.resources.fav_pt
import com.rainccup.aktool.resources.level
import com.rainccup.aktool.resources.potential_rank
import com.rainccup.aktool.resources.save
import com.rainccup.aktool.resources.skill_level
import com.rainccup.aktool.resources.star_mark
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CharacterDetailPage(
    character: Character,
    onSaved: (Character?) -> Unit,
    vm: CharacterDetailViewModel = koinViewModel { parametersOf(character.new()) },
) {
    val char by vm.character.collectAsState()
    val maxLevel by vm.maxLevel.collectAsState()
    val maxSkillLevel by vm.maxSkillLevel.collectAsState()
    val maxEvoPhase by vm.maxEvoPhase.collectAsState()
    val baseDensity = LocalDensity.current
    val uiScale = platformUiScale()
    val saveChar: () -> Unit = {
        val ruled = vm.applySaveRules(char)
        vm.saveCharData(ruled)
        onSaved(ruled)
    }
    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        baseDensity.density * uiScale,
                        baseDensity.fontScale
                    )
                ) {
                    Row(modifier = Modifier.padding(start = 4.dp).fillMaxWidth()) {
                        CharacterCard(
                            modifier = Modifier.characterCardSharedElement(char.instId),
                            char = char,
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.height(220.dp)
                        ) {
                            IntRangeSlider(
                                value = char.potentialRank,
                                start = 0,
                                maxValue = 5,
                                title = stringResource(Res.string.potential_rank),
                                onValueChangeFinished = { vm.accept(char.copy(potentialRank = it)) },
                                modifier = Modifier.weight(1f)
                            )
                            IntRangeSlider(
                                value = char.favorPoint,
                                start = 0,
                                maxValue = 200,
                                title = stringResource(Res.string.fav_pt),
                                onValueChangeFinished = { vm.accept(char.copy(favorPoint = it)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IntRangeSlider(
                            value = char.evolvePhase,
                            start = 0,
                            maxValue = maxEvoPhase,
                            title = stringResource(Res.string.evp_phase),
                            onValueChangeFinished = { vm.changeEvoPhase(it) },
                            modifier = Modifier.weight(1f)
                        )
                        IntRangeSlider(
                            value = char.level,
                            start = 1,
                            maxValue = maxLevel,
                            title = stringResource(Res.string.level),
                            onValueChangeFinished = { vm.accept(char.copy(level = it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (char.skills.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .roundedBackground()
                        ) {
                            IntRangeSlider(
                                value = char.mainSkillLvl,
                                start = 1,
                                maxValue = maxSkillLevel,
                                title = stringResource(Res.string.skill_level),
                                background = Color.Transparent,
                                onValueChangeFinished = { vm.accept(char.copy(mainSkillLvl = it)) }
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .height(80.dp)
                            ) {
                                itemsIndexed(char.skills) { index, skill ->
                                    SkillDetail(
                                        skillId = skill.skillId,
                                        unlock = skill.unlock == 1,
                                        showSpecialLevel = char.evolvePhase >= 2 && char.mainSkillLvl >= 7,
                                        specializeLevel = skill.specializeLevel,
                                        select = index == char.defaultSkillIndex,
                                        onSelectedChange = {
                                            if (skill.unlock == 1) {
                                                vm.accept(char.copy(defaultSkillIndex = index))
                                            }
                                        },
                                        onSpecialLevelChange = { level ->
                                            val copy = skill.copy(specializeLevel = level)
                                            val skills = char.skills.toMutableList()
                                            vm.accept(char.copy(skills = skills.also {
                                                it[index] = copy
                                            }))
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (char.equip.isNotEmpty()) {
                        CharEquip(
                            evolvePhase = char.evolvePhase,
                            currentEquip = char.currentEquip,
                            equips = char.equip,
                            onEquipIdChange = { vm.accept(char.copy(currentEquip = it)) },
                            onEquipChange = { vm.accept(char.copy(equip = it)) }
                        )
                    }
                    SwitchPreference(
                        title = stringResource(Res.string.star_mark),
                        checked = char.starMark == 1,
                        onCheckedChange = { vm.accept(char.copy(starMark = if (it) 1 else 0)) },
                        modifier = Modifier
                            .padding(4.dp)
                            .roundedBackground()
                    )
                }
            }
        }
        CompositionLocalProvider(
            LocalDensity provides Density(baseDensity.density * uiScale, baseDensity.fontScale)
        ) {
            PageActionRow(
                confirmText = stringResource(Res.string.save),
                onConfirm = saveChar,
                onCancel = { onSaved(null) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun SkillDetail(
    skillId: String = "",
    unlock: Boolean = true,
    showSpecialLevel: Boolean = false,
    specializeLevel: Int = 0,
    select: Boolean = true,
    onSelectedChange: () -> Unit = {},
    onSpecialLevelChange: (Int) -> Unit = {}
) {
    Row(modifier = Modifier.padding(8.dp)) {
        Box(modifier = Modifier.clickable { onSelectedChange() }) {
            val skillPainter = if (unlock) {
                skillPainter(skillId)
            } else {
                painterResource(Res.drawable.character_locked_skill)
            }
            Image(
                painter = skillPainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (select) 1f else 0f,
                        ),
                    )
            )
            if (showSpecialLevel) {
                val specialLevelPainter = when (specializeLevel) {
                    1 -> Res.drawable.character_special_skill_1
                    2 -> Res.drawable.character_special_skill_2
                    3 -> Res.drawable.character_special_skill_3
                    else -> Res.drawable.character_special_skill_0
                }
                Image(
                    painter = painterResource(specialLevelPainter),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .width(16.dp)
                )
            }
            if (select) {
                Image(
                    painter = painterResource(Res.drawable.character_skill_selected),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(alignment = Alignment.TopEnd)
                        .width(24.dp)
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxHeight()
                .width(28.dp)
        ) {
            IconButton(
                onClick = { onSpecialLevelChange(specializeLevel + 1) },
                enabled = showSpecialLevel && specializeLevel < 3,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "+",
                    color = MiuixTheme.colorScheme.onBackground
                )
            }
            IconButton(
                onClick = { onSpecialLevelChange(specializeLevel - 1) },
                enabled = showSpecialLevel && specializeLevel > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "-",
                    color = MiuixTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun CharEquip(
    evolvePhase: Int,
    currentEquip: String?,
    equips: Map<String, Equip>,
    onEquipIdChange: (String) -> Unit,
    onEquipChange: (Map<String, Equip>) -> Unit,
) {
    BasicComponent(
        title = stringResource(Res.string.equip),
        modifier = Modifier.padding(4.dp).roundedBackground(),
        bottomAction = {
            LazyRow(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(4.dp)
            ) {
                items(equips.toList()) { (equipId, equip) ->
                    EquipDetail(
                        equipId = equipId,
                        level = equip.level,
                        locked = equip.locked,
                        isSelect = currentEquip == equipId,
                        onSelectedChange = {
                            if (evolvePhase >= 2) {
                                onEquipIdChange(equipId)
                            }
                        },
                        onLevelChange = { level ->
                            val copy = equip.copy(level = level)
                            val map = equips.toMutableMap().also {
                                it[equipId] = copy
                            }
                            onEquipChange(map)
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun EquipDetail(
    equipId: String,
    level: Int,
    locked: Int,
    isSelect: Boolean = false,
    onSelectedChange: () -> Unit = { },
    onLevelChange: (Int) -> Unit = { }
) {
    val gameTable: GameTableQuery = koinInject()
    val equipType = gameTable.getEquipType(equipId)
    val isOriginal = equipType == "original"
    Row(modifier = Modifier.padding(8.dp)) {
        Box(
            modifier = Modifier
                .clickable { onSelectedChange() }
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = equipPainter(equipType),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = if (isOriginal) ColorFilter.tint(MiuixTheme.colorScheme.onBackground) else null,
                modifier = Modifier
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (isSelect) 1f else 0f,
                        ),
                    )
                    .fillMaxHeight()
                    .width(60.dp)
                    .padding(start = 4.dp)
            )
            if (locked == 1) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else if (isOriginal.not() && locked == 0) {
                val specialLevelPainter = when (level) {
                    1 -> Res.drawable.character_special_skill_1
                    2 -> Res.drawable.character_special_skill_2
                    3 -> Res.drawable.character_special_skill_3
                    else -> Res.drawable.character_special_skill_0
                }
                Image(
                    painter = painterResource(specialLevelPainter),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .width(16.dp)
                )
            }
            if (isSelect) {
                Image(
                    painter = painterResource(Res.drawable.character_skill_selected),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(24.dp)
                )
            }
        }
        if (!isOriginal) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(28.dp)
            ) {
                IconButton(
                    onClick = { onLevelChange(level + 1) },
                    enabled = locked == 0 && level < 3,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "+",
                        color = MiuixTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = { onLevelChange(level - 1) },
                    enabled = locked == 0 && level > 1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "-",
                        color = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}






