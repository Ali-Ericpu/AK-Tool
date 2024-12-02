package org.doctorate.aktool.ui.page.characterdetail

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.doctorate.aktool.R
import org.doctorate.aktool.config.Table
import org.doctorate.aktool.pojo.entity.Character
import org.doctorate.aktool.pojo.entity.Skill
import org.doctorate.aktool.ui.page.character.CharacterCard
import org.doctorate.aktool.ui.page.character.CharacterViewModel
import org.doctorate.aktool.ui.page.character.skillPainter
import org.doctorate.aktool.ui.page.setting.EditSwitch
import org.doctorate.aktool.utils.JsonUtil
import org.doctorate.aktool.utils.replace
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun CharacterDetail(
    vm: CharacterDetailViewModel = viewModel(),
    charViewModel: CharacterViewModel = viewModel(),
    onCharSave: () -> Unit
) {
    val context = LocalContext.current
    val charInstId by vm.charInstId.collectAsState()
    val character = charViewModel.characterData[charInstId] ?: run {
        Toast.makeText(context, R.string.error_data, Toast.LENGTH_SHORT).show()
        onCharSave()
        Character.char()
    }
    var char by rememberSaveable {
        mutableStateOf(character.copy(
            favorPoint = Table.getFavPointPercent(character.favorPoint),
            skills = character.skills.map { it.copy() }
        ))
    }
    var skills = remember { mutableStateListOf<Skill>().apply { addAll(char.skills) } }
    var maxLevel by remember {
        mutableIntStateOf(Table.getMaxCharLevel(char.charId, char.evolvePhase))
    }
    var maxSkillLevel by remember { mutableIntStateOf(if (char.evolvePhase < 1) 4 else 7) }
    val maxEvoLevel by remember { mutableIntStateOf(Table.getMaxCharEvoLevel(character.charId)) }
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(modifier = Modifier.fillMaxSize().alpha(0.95f)) {
        item {
            Row(modifier = Modifier.padding(start = 4.dp)) {
                CharacterCard(char)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.height(220.dp)
                ) {
                    IntRangeSlider(
                        value = char.potentialRank.toFloat(),
                        start = 0,
                        maxValue = 5,
                        description = stringResource(R.string.potential_rank),
                        onValueChange = { char = char.copy(potentialRank = it.roundToInt()) },
                        modifier = Modifier.weight(1f)
                    )
                    IntRangeSlider(
                        value = char.favorPoint.toFloat(),
                        start = 0,
                        maxValue = 200,
                        description = stringResource(R.string.fav_pt),
                        onValueChange = { char = char.copy(favorPoint = it.roundToInt()) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                IntRangeSlider(
                    value = char.evolvePhase.toFloat(),
                    start = 0,
                    maxValue = maxEvoLevel,
                    description = stringResource(R.string.evp_phase),
                    onValueChange = { char = char.copy(evolvePhase = it.roundToInt()) },
                    onValueChangeFinished = { phase ->
                        skills.clear()
                        char.skills.forEachIndexed { index, skill ->
                            if (phase >= index) {
                                skill.unlock = 1
                                char.defaultSkillIndex = index
                            } else {
                                skill.unlock = 0
                            }
                            skills.add(skill)
                        }
                        if (char.skills.getOrNull(char.defaultSkillIndex) == null) {
                            char.skills.forEachIndexed { index, skill ->
                                if (skill.unlock == 1) {
                                    char.defaultSkillIndex = index
                                }
                            }
                        }
                        maxLevel = Table.getMaxCharLevel(char.charId, phase)
                        maxSkillLevel = if (phase < 1) 4 else 7
                        char = (char.copy(
                            mainSkillLvl = min(maxSkillLevel, char.mainSkillLvl),
                            level = min(maxLevel, char.level)
                        ))
                    },
                    modifier = Modifier.weight(1f)
                )
                IntRangeSlider(
                    value = char.level.toFloat(),
                    start = 1,
                    maxValue = maxLevel,
                    description = stringResource(R.string.level),
                    onValueChange = { char = char.copy(level = it.roundToInt()) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (skills.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    IntRangeSlider(
                        value = char.mainSkillLvl.toFloat(),
                        start = 1,
                        maxValue = maxSkillLevel,
                        description = stringResource(R.string.skill_level),
                        onValueChange = { char = char.copy(mainSkillLvl = it.roundToInt()) },
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
                            .height(80.dp)
                    ) {
                        items(skills.size) { index ->
                            val skill = skills[index]
                            SkillDetail(
                                char.evolvePhase,
                                char.mainSkillLvl,
                                skill,
                                char.defaultSkillIndex == index,
                                onSelectedChange = {
                                    if (skill.unlock == 1) {
                                        char.defaultSkillIndex = index
                                        char = char.copy(defaultSkillIndex = index)
                                    }
                                    skills.replace(char.skills)
                                },
                                onSpecialLevelChange = {
                                    skill.specializeLevel = it
                                }
                            )
                        }
                    }
                }
            }
            EditSwitch(
                label = stringResource(R.string.star_mark),
                state = char.starMark == 1,
                onCheckedChange = { char = char.copy(starMark = if (it) 1 else 0) },
            )
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { onCharSave() }) {
                    Text(stringResource(R.string.cancel))
                }
                Button(onClick = {
                    if (char.evolvePhase == 2 && char.equip.isNotEmpty()) {
                        val first = char.equip.keys.first()
                        if (char.currentEquip == null) {
                            char.currentEquip = first
                        }
                        char.equip.values.forEach {
                            if (it.locked == 1) {
                                it.unlock()
                            }
                        }
                    } else if (char.evolvePhase < 2) {
                        char.currentTmpl = null
                        char.tmpl?.clear()
                        char.currentEquip = null
                        char.equip.values.forEach { it.lock() }
                        char.skills.forEach { it.specializeLevel = 0 }
                    }
                    if (char.level == maxLevel) {
                        char.exp = 0
                    }
                    char.favorPoint = Table.getRealFavPoint(char.favorPoint)
                    Log.d("CharData", "CharacterDetail: ${JsonUtil.toPrettyJson(char)}")
                    coroutineScope.launch {
                        runCatching {
                            charViewModel.changeCharData(char)
                        }.onSuccess {
                            Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT)
                                .show()
                            onCharSave()
                        }.onFailure {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun IntRangeSlider(
    value: Float = 10f,
    start: Int = 0,
    maxValue: Int = 99,
    description: String = "Test",
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit = { },
    onValueChangeFinished: (Int) -> Unit = { },
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
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
                fontSize = 24.sp,
                color = Color.Black,
            )
            Text(
                text = value.roundToInt().toString(),
                fontSize = 24.sp,
                color = Color.Black,
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
                inactiveTickColor = Color.White.copy(alpha = 0f),
                activeTickColor = Color.White.copy(alpha = 0f),
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
        )
    }
}

@Composable
fun SkillDetail(
    evoPhase: Int = 2,
    skillLevel: Int = 7,
    skill: Skill = Skill("114514", 1, 1, 3, -1),
    select: Boolean = true,
    onSelectedChange: (Boolean) -> Unit = {},
    onSpecialLevelChange: (Int) -> Unit = {}
) {
    var specializeLevel by remember { mutableIntStateOf(skill.specializeLevel) }
    Row(modifier = Modifier.padding(8.dp)) {
        Box(modifier = Modifier.clickable { onSelectedChange(true) }) {
            val skillPainter = if (skill.unlock == 0) {
                painterResource(R.drawable.character_locked_skill)
            } else {
                skillPainter(skill.skillId)
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
            if (evoPhase >= 2 && skillLevel == 7) {
                val specialLevelPainter = when (specializeLevel) {
                    1 -> R.drawable.character_special_skill_1
                    2 -> R.drawable.character_special_skill_2
                    3 -> R.drawable.character_special_skill_3
                    else -> R.drawable.character_special_skill_0
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
                    painter = painterResource(R.drawable.character_skill_selected),
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
                onClick = { onSpecialLevelChange(++specializeLevel) },
                enabled = evoPhase >= 2 && skillLevel >= 7 && specializeLevel < 3,
                modifier = Modifier.weight(1f)
            ) {
                Text("+")
            }
            IconButton(
                onClick = { onSpecialLevelChange(--specializeLevel) },
                enabled = evoPhase >= 2 && skillLevel >= 7 && specializeLevel > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text("-")
            }
        }
    }
}
