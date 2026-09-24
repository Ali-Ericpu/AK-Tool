package com.rainccup.aktool.ui.characterdetail

import org.koin.compose.koinInject


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Slider
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.*
import com.rainccup.aktool.core.datastore.GameTableRepository
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.ui.character.CharacterCard
import com.rainccup.aktool.ui.character.CharacterViewModel
import com.rainccup.aktool.ui.character.equipPainter
import com.rainccup.aktool.ui.character.skillPainter
import com.rainccup.aktool.ui.setting.EditSwitch
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun CharacterDetailPage(
    charInstId: String,
    onSaved: () -> Unit,
    vm: CharacterDetailViewModel = koinViewModel { parametersOf(charInstId) },
    charViewModel: CharacterViewModel = koinViewModel(),
) {
    val gameTable: GameTableRepository = koinInject()
    val character = charViewModel.characterData[charInstId] ?: run {
        onSaved()
        Character.placeholder()
    }
    LaunchedEffect(Unit) {
        val copy = character.copy(
            favorPoint = gameTable.getFavPointPercent(character.favorPoint),
            skills = character.skills.map { it.copy() }
        )
        vm.updateMaxEvoPhase(copy.charId)
        vm.updateMaxSkillLevel(copy.evolvePhase)
        vm.updateMaxLevel(copy.charId, copy.evolvePhase)
        vm.accept(copy)
    }
    val char by vm.character.collectAsState()
    val maxLevel by vm.maxLevel.collectAsState()
    val maxSkillLevel by vm.maxSkillLevel.collectAsState()
    val maxEvoPhase by vm.maxEvoPhase.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.95f)
    ) {
        item {
            Row(modifier = Modifier.padding(start = 4.dp)) {
                CharacterCard(char = char)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.height(220.dp)
                ) {
                    IntRangeSlider(
                        value = char.potentialRank.toFloat(),
                        start = 0,
                        maxValue = 5,
                        description = stringResource(Res.string.potential_rank),
                        onValueChange = { vm.accept(char.copy(potentialRank = it.roundToInt())) },
                        modifier = Modifier.weight(1f)
                    )
                    IntRangeSlider(
                        value = char.favorPoint.toFloat(),
                        start = 0,
                        maxValue = 200,
                        description = stringResource(Res.string.fav_pt),
                        onValueChange = { vm.accept(char.copy(favorPoint = it.roundToInt())) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                IntRangeSlider(
                    value = char.evolvePhase.toFloat(),
                    start = 0,
                    maxValue = maxEvoPhase,
                    description = stringResource(Res.string.evp_phase),
                    onValueChange = { vm.changeEvoPhase(it.roundToInt()) },
                    modifier = Modifier.weight(1f)
                )
                IntRangeSlider(
                    value = char.level.toFloat(),
                    start = 1,
                    maxValue = maxLevel,
                    description = stringResource(Res.string.level),
                    onValueChange = { vm.accept(char.copy(level = it.roundToInt())) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (char.skills.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    IntRangeSlider(
                        value = char.mainSkillLvl.toFloat(),
                        start = 1,
                        maxValue = maxSkillLevel,
                        description = stringResource(Res.string.skill_level),
                        onValueChange = { vm.accept(char.copy(mainSkillLvl = it.roundToInt())) },
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
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
                                    vm.accept(char.copy(skills = skills.also { it[index] = copy }))
                                }
                            )
                        }
                    }
                }
            }
            if (char.equip.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    Text(
                        text = stringResource(Res.string.equip),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(4.dp)
                    ) {
                        items(char.equip.toList()) { (equipId, equipData) ->
                            EquipDetail(
                                equipId = equipId,
                                level = equipData.level,
                                locked = equipData.locked,
                                isSelect = char.currentEquip == equipId,
                                onSelectedChange = {
                                    if (char.evolvePhase >= 2) {
                                        vm.accept(char.copy(currentEquip = equipId))
                                    }
                                },
                                onLevelChange = { level ->
                                    val copy = equipData.copy(level = level)
                                    val map = char.equip.toMutableMap()
                                    vm.accept(char.copy(equip = map.also { it[equipId] = copy }))
                                }
                            )
                        }
                    }
                }
            }
            EditSwitch(
                label = stringResource(Res.string.star_mark),
                state = char.starMark == 1,
                onCheckedChange = { vm.accept(char.copy(starMark = if (it) 1 else 0)) },
            )
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { onSaved() }) {
                    Text(stringResource(Res.string.cancel))
                }
                Button(onClick = {
                    if (char.evolvePhase == 2) {
                        if (char.equip.isNotEmpty()) {
                            val first = char.equip.keys.first()
                            if (char.currentEquip == null) {
                                vm.accept(char.copy(currentEquip = first))
                            }
                            char.equip.values.forEach {
                                if (it.locked == 1) {
                                    it.unlock()
                                }
                            }
                        }
                        if (char.skin == char.charId + "#1" &&
                            gameTable.characterTable[char.charId]!!["displayNumber"] != null
                        ) {
                            vm.accept(char.copy(skin = char.charId + "#2"))
                        }
                    } else if (char.evolvePhase < 2) {
                        vm.accept(
                            char.copy(
                                currentTmpl = null,
                                tmpl = null,
                                currentEquip = null,
                                equip = char.equip.mapValues { it.value.copy() },
                                skills = char.skills.map { it.copy(specializeLevel = 0) }
                            )
                        )
                    }
                    if (char.level == maxLevel) {
                        vm.accept(char.copy(exp = 0))
                    }
                    Logger.d { "CharacterDetail" }
                    coroutineScope.launch {
                        runCatching {
                            charViewModel.changeCharData(
                                char.copy(favorPoint = gameTable.getFavPointPercent(char.favorPoint))
                            )
                        }.onSuccess {
                            onSaved()
                        }.onFailure {

                        }
                    }
                }) {
                    Text(stringResource(Res.string.save))
                }
            }
        }
    }
}

@Composable
fun IntRangeSlider(
    value: Float = 10f,
    start: Int = 0,
    maxValue: Int = 99,
    description: String = "Test",
    textColor: Color = Color.Black,
    color: Color = Color.LightGray,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit = { },
    onValueChangeFinished: (Int) -> Unit = { },
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = description,
                fontSize = 20.sp,
                color = textColor,
            )
            Text(
                text = value.roundToInt().toString(),
                fontSize = 20.sp,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
        Slider(
            value = value,
            onValueChange = { onValueChange(it) },
            onValueChangeFinished = { onValueChangeFinished(value.roundToInt()) },
            valueRange = start.toFloat()..maxValue.toFloat(),
            steps = max(maxValue - start - 1, 0),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTickColor = Color.Unspecified,
                activeTickColor = Color.Unspecified,
            ),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
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
                Text("+")
            }
            IconButton(
                onClick = { onSpecialLevelChange(specializeLevel - 1) },
                enabled = showSpecialLevel && specializeLevel > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text("-")
            }
        }
    }
}

@Composable
fun EquipDetail(
    equipId: String = "",
    level: Int = 1,
    locked: Int = 1,
    isSelect: Boolean = false,
    onSelectedChange: () -> Unit = { },
    onLevelChange: (Int) -> Unit = { }
) {
    val gameTable: GameTableRepository = koinInject()
    val equipType = gameTable.getEquipType(equipId)
    val isOriginal = equipType == "original"
    Row(modifier = Modifier.padding(8.dp)) {
        ConstraintLayout(
            modifier = Modifier
                .clickable { onSelectedChange() }
                .fillMaxHeight()
        ) {
            val (equipIconRef, levelRef, selectRef, lockedRef) = createRefs()
            Image(
                painter = equipPainter(equipType),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = if (isOriginal) ColorFilter.tint(Color.Black) else null,
                modifier = Modifier
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (isSelect) 1f else 0f,
                        ),
                    )
                    .constrainAs(equipIconRef) {
                        centerTo(parent)
                    }
                    .fillMaxHeight()
                    .width(60.dp)
                    .padding(start = 4.dp)
            )
            if (locked == 1) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.constrainAs(lockedRef) {
                        centerTo(equipIconRef)
                    }
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
                        .width(16.dp)
                        .constrainAs(levelRef) {
                            top.linkTo(equipIconRef.top)
                            start.linkTo(equipIconRef.start)
                        }
                )
            }
            if (isSelect) {
                Image(
                    painter = painterResource(Res.drawable.character_skill_selected),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(24.dp)
                        .constrainAs(selectRef) {
                            top.linkTo(equipIconRef.top)
                            end.linkTo(equipIconRef.end)
                        }
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
                onClick = { onLevelChange(level + 1) },
                enabled = isOriginal.not() && locked == 0 && level < 3,
                modifier = Modifier.weight(1f)
            ) {
                Text("+")
            }
            IconButton(
                onClick = { onLevelChange(level - 1) },
                enabled = isOriginal.not() && locked == 0 && level > 1,
                modifier = Modifier.weight(1f)
            ) {
                Text("-")
            }
        }
    }
}






