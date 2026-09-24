package com.rainccup.aktool.ui.character
import org.koin.compose.koinInject

import com.rainccup.aktool.core.platform.urlEncode


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import org.koin.compose.viewmodel.koinViewModel
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.*
import com.rainccup.aktool.core.datastore.GameTableRepository
import com.rainccup.aktool.ui.character.CharPainter
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.Profession
import com.rainccup.aktool.ui.setting.BasicDialog
import com.rainccup.aktool.ui.setting.EditTextDialog
import com.rainccup.aktool.ui.splash.CircleIconButton
import com.rainccup.aktool.utils.replace

import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterPage(onOpenDetail: (String) -> Unit) {
    val viewModel: CharacterViewModel = koinViewModel()
    val gameTable: GameTableRepository = koinInject()
        val charList = viewModel.charList()
    val splash by viewModel.splash.collectAsState()
    val showLoadAnimate by viewModel.loadAnimate.collectAsState()
    val showGainCharDialog by viewModel.gainChar.collectAsState()
    val currentProfession by viewModel.profession.collectAsState()
    val selectProfession by viewModel.isSelect.collectAsState()
    val showSearchDialog by viewModel.isSearch.collectAsState()
    val professions = Profession.entries.toList()
    val coroutineScope = rememberCoroutineScope()
    val professionOffsetX by animateFloatAsState(if (selectProfession) 0f else 1.2f, label = "")
    val menuOffsetX by animateFloatAsState(if (!selectProfession) 0f else 1.5f, label = "")
    val lazyGridState = rememberLazyGridState()
    val refresh: () -> Unit = { viewModel.initCharData() }
    PullToRefreshBox(
        isRefreshing = showLoadAnimate,
        onRefresh = { refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (!showLoadAnimate && !splash) {
            LazyVerticalGrid(
                GridCells.FixedSize(108.dp),
                verticalArrangement = Arrangement.Top,
                horizontalArrangement = Arrangement.SpaceAround,
                state = lazyGridState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(charList) { char ->
                    CharacterCard(char = char) {
                        onOpenDetail(it.toString())
                    }
                }
            }
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .offsetPercent(offsetPercentX = professionOffsetX)
                ) {
                    item {
                        IconButton(
                            onClick = {
                                if (currentProfession == "ALL") {
                                    viewModel.changeSelectState(false)
                                } else {
                                    viewModel.selectProfession("ALL")
                                    coroutineScope.launch { lazyGridState.scrollToItem(0) }
                                }
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.7f))
                                .align(alignment = Alignment.Center)
                        ) {
                            Text(
                                text = if (currentProfession == "ALL") "BACK" else "ALL",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    items(professions) {
                        Box {
                            Image(
                                painter = painterResource(it.icon),
                                contentDescription = it.name,
                                modifier = Modifier
                                    .alpha(0.7f)
                                    .clickable {
                                        viewModel.selectProfession(it.name)
                                        coroutineScope.launch { lazyGridState.scrollToItem(0) }
                                    }
                            )
                            if (currentProfession == it.name) {
                                Image(
                                    painter = painterResource(Res.drawable.profession_select),
                                    contentDescription = "select",
                                    alignment = Alignment.CenterEnd,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.offsetPercent(offsetPercentX = menuOffsetX)) {
                    CircleIconButton(
                        icon = Icons.Default.Menu,
                        onClick = { viewModel.changeSelectState(true) }
                    )
                    CircleIconButton(
                        icon = Icons.Default.Add,
                        onClick = { viewModel.changeGainCharState() }
                    )
                    CircleIconButton(
                        icon = Icons.Default.Search,
                        onClick = { viewModel.changeSearchState() }
                    )
                    CircleIconButton(
                        icon = Icons.Default.Refresh,
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    refresh()
                                } catch (_: Exception) {
                                    viewModel.closeAnimate()
                                }
                            }
                        }
                    )
                }
            }
        }
        if (splash && !showLoadAnimate) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (gameTable.init()) {
                                refresh()
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .size(72.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.baseline_start),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
    if (showGainCharDialog) {
        EditTextDialog(
            label = stringResource(Res.string.char_id),
            error = { !it.startsWith("char_") || gameTable.characterTable[it] == null },
            onValueSave = { charId ->
                charId?.let {
                    coroutineScope.launch {
                        runCatching {
                            viewModel.gainChar(charId)
                        }.onSuccess {
                            
                            refresh()
                        }.onFailure {
                            
                        }
                    }
                }
                viewModel.changeGainCharState()
            }
        )
    }
    if (showSearchDialog) {
        SearchCharDialog(
            onKeywordType = { viewModel.getSearchedCharList(it) },
            onSearchCharId = { viewModel.getCharIdByCharName(it) }
        ) {
            it?.let { viewModel.searchChar(it) }
            viewModel.changeSearchState()
        }
    }

}

@Composable
fun CharacterCard(
    modifier: Modifier = Modifier,
    char: Character = Character.placeholder(),
    onCharSelect: (Int) -> Unit = { }
) {
    val gameTable: GameTableRepository = koinInject()
    val evolvePhasePainter = when (char.evolvePhase) {
        0 -> Res.drawable.character_elite_0
        1 -> Res.drawable.character_elite_1
        2 -> Res.drawable.character_elite_2
        else -> Res.drawable.character_elite_0
    }
    val charPainter = CharPainter.form(char.rank!!)
    val profession = Profession.valueOf(char.profession!!)
    Box(
        modifier = modifier
            .height(228.dp)
            .width(108.dp)
            .clickable { (onCharSelect(char.instId)) }
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (charBgRef, portraitRef, topHubRef, bottomHubRef, charNameRef, rarityLightRef, professionRef, equipRef) = remember { createRefs() }
            val (potentialBgRef, starRef, evoRef, evoBgRef, levelRef, lvRef, levelBgRef, skillRef, potentialRef, starMarkRef) = remember { createRefs() }
            //char bg
            Image(
                painter = painterResource(charPainter.charBgPainter),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .fillMaxWidth()
                    .constrainAs(charBgRef) {
                        bottom.linkTo(parent.bottom, 22.dp)
                    }
            )
            //char skin
            Image(
                painter = portraitPainter(char.skin),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .fillMaxWidth()
                    .constrainAs(portraitRef) {
                        top.linkTo(topHubRef.top)
                    }
            )
            //rarity light
            Image(
                painter = painterResource(charPainter.rarityLightPainter),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .fillMaxWidth()
                    .constrainAs(rarityLightRef) {
                        bottom.linkTo(charBgRef.bottom)
                    }
            )
            //top hub
            Image(
                painter = painterResource(charPainter.upperHubPainter),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(24.dp)
                    .constrainAs(topHubRef) {
                        start.linkTo(parent.start, 4.dp)
                        top.linkTo(parent.top, 4.dp)
                    }
            )
            //profession icon
            Image(
                painterResource(profession.icon), null,
                modifier = Modifier
                    .size(18.dp)
                    .constrainAs(professionRef) {
                        start.linkTo(topHubRef.start, 4.dp)
                        top.linkTo(topHubRef.top, 4.dp)
                    }
            )
            //char rarity star
            Image(
                painterResource(charPainter.rarityPainter), null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(18.dp)
                    .constrainAs(starRef) {
                        start.linkTo(professionRef.end, 2.dp)
                        top.linkTo(professionRef.top)
                    }
            )
            //evolvePhase icon bg
            Image(
                painterResource(Res.drawable.character_elite_bg), null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(48.dp)
                    .alpha(0.7f)
                    .constrainAs(evoBgRef) {
                        bottom.linkTo(evoRef.bottom, (-12).dp)
                        centerHorizontallyTo(evoRef)
                    }
            )
            //star mark
            val starMarkPainter = if (char.starMark == 1) Res.drawable.character_star_mark
            else Res.drawable.character_star_mark_edit
            Image(
                painterResource(starMarkPainter),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(24.dp)
                    .constrainAs(starMarkRef) {
                        start.linkTo(parent.start, 4.dp)
                        bottom.linkTo(parent.bottom, 4.dp)
                    }
            )
            //lower hub
            Image(
                painterResource(charPainter.lowerHubPainter), null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(bottomHubRef) {
                        bottom.linkTo(parent.bottom)
                    }
            )
            //char evolvePhase
            Image(
                painterResource(evolvePhasePainter), null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .constrainAs(evoRef) {
                        bottom.linkTo(levelBgRef.top, (-10).dp)
                        centerHorizontallyTo(levelRef)
                    }
            )
            //char level
            Image(
                painterResource(Res.drawable.character_level_bg), null,
                modifier = Modifier
                    .size(46.dp)
                    .constrainAs(levelBgRef) {
                        start.linkTo(parent.start, 2.dp)
                        bottom.linkTo(charNameRef.top, (-8).dp)
                    }
            )
            Text(
                text = char.level.toString(),
                color = Color.White,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(50.dp)
                    .constrainAs(levelRef) {
                        centerTo(levelBgRef)
                        bottom.linkTo(levelBgRef.bottom, (-6).dp)
                    }
                    .alpha(0.9f)
            )
            Text(
                text = stringResource(Res.string.lv),
                color = Color.White,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 6.sp
                ),
                modifier = Modifier.constrainAs(lvRef) {
                    centerTo(levelBgRef)
                    bottom.linkTo(levelRef.top, (-12).dp)
                }
            )
            //char skill
            val skill = char.skills.getOrNull(char.defaultSkillIndex)
            val skillPainter = if (skill == null) {
                if (char.currentTmpl == null) {
                    painterResource(Res.drawable.character_empty_skill)
                } else {
                    val tmplChar = char.tmpl!![char.currentTmpl]
                    val tmplSkill = tmplChar?.skills?.getOrNull(tmplChar.defaultSkillIndex)
                    if (tmplSkill == null) {
                        painterResource(Res.drawable.character_empty_skill)
                    } else {
                        skillPainter(tmplSkill.skillId)
                    }
                }
            } else {
                skillPainter(skill.skillId)
            }
            Image(
                painter = skillPainter,
                contentDescription = null,
                modifier = Modifier
                    .size(26.dp)
                    .constrainAs(skillRef) {
                        end.linkTo(charNameRef.end)
                        bottom.linkTo(charNameRef.top)
                    }
            )
            //equip
            char.currentEquip?.let { equipId ->
                Image(
                    painter = equipPainter(gameTable.getEquipType(equipId)),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).constrainAs(equipRef) {
                        centerVerticallyTo(skillRef)
                        end.linkTo(skillRef.start)
                    }
                )
            }
            //char name
            Text(
                char.name!!,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.constrainAs(charNameRef) {
                    end.linkTo(parent.end, 8.dp)
                    bottom.linkTo(parent.bottom, 4.dp)
                }
            )
            //char potentialRank
            if (char.potentialRank > 0) {
                val potentialPainter = when (char.potentialRank) {
                    1 -> Res.drawable.character_potential_1
                    2 -> Res.drawable.character_potential_2
                    3 -> Res.drawable.character_potential_3
                    4 -> Res.drawable.character_potential_4
                    else -> Res.drawable.character_potential_5
                }
                Image(
                    painterResource(Res.drawable.equip_bg), null,
                    modifier = Modifier
                        .size(16.dp)
                        .constrainAs(potentialBgRef) {
                            bottom.linkTo(skillRef.top, 4.dp)
                            end.linkTo(skillRef.end)
                        }
                )
                Image(
                    painterResource(potentialPainter), null,
                    modifier = Modifier
                        .size(24.dp)
                        .constrainAs(potentialRef) {
                            centerTo(potentialBgRef)
                        }
                )
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchCharDialog(
    onKeywordType: (String) -> List<String> = { listOf() },
    onSearchCharId: (String) -> String = { "" },
    onConfirmKeyword: (String?) -> Unit = { }
) {
    val clipboard = koinInject<com.rainccup.aktool.core.platform.ClipboardPort>()
    var keyword by remember { mutableStateOf("") }
    val charNameList = remember { mutableStateListOf<String>() }
    var selectedKeyword by remember { mutableStateOf("") }
    BasicDialog(
        label = stringResource(Res.string.search),
        onCancel = { onConfirmKeyword(null) },
        onConfirm = { onConfirmKeyword(selectedKeyword) }
    ) {
        OutlinedTextField(
            value = keyword,
            maxLines = Int.MAX_VALUE,
            onValueChange = {
                keyword = it
                charNameList.replace(onKeywordType(keyword))
            },
            modifier = Modifier.fillMaxWidth()
        )
        FlowRow(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            charNameList.forEach { word ->
                val message = stringResource(Res.string.copy_success)
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedKeyword == word) MaterialTheme.colorScheme.primary else Color.LightGray)
                        .combinedClickable(
                            enabled = true,
                            onClick = {
                                selectedKeyword = word
                                keyword = word
                            },
                            onLongClick = {
                                val charId = onSearchCharId(word)
                                clipboard.setText(charId)
                            }
                        )
                        .padding(8.dp)
                ) {
                    Text(text = word, color = Color.Black)
                }
            }
        }
    }
}

fun Modifier.offsetPercent(offsetPercentX: Float = 0f, offsetPercentY: Float = 0f): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            val offsetX = (offsetPercentX * placeable.width).roundToInt()
            val offsetY = (offsetPercentY * placeable.height).roundToInt()
            placeable.place(offsetX, offsetY)
        }
    }

@Composable
fun portraitPainter(skinId: String): Painter {
    val encode = urlEncode(skinId)
    val imageUrl = "https://web.hycdn.cn/arknights/game/assets/char_skin/portrait/$encode.png"
    return rememberAsyncImagePainter(model = imageUrl)
}

@Composable
fun skillPainter(skillId: String): Painter {
    val encode = urlEncode(skillId)
    val imageUrl = "https://web.hycdn.cn/arknights/game/assets/char_skill/$encode.png"
    return rememberAsyncImagePainter(
        model = imageUrl,
        placeholder = painterResource(Res.drawable.character_default_skill_icon),
        error = painterResource(Res.drawable.character_default_skill_icon),
    )
}

@Composable
fun equipPainter(equipId: String): Painter {
    val imageUrl = "https://web.hycdn.cn/arknights/game/assets/uniequip/type/$equipId.png"
    return rememberAsyncImagePainter(model = imageUrl)
}







