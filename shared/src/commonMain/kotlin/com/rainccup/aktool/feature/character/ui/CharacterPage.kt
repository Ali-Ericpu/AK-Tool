package com.rainccup.aktool.feature.character.ui


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.rainccup.aktool.core.common.replace
import com.rainccup.aktool.core.designsystem.component.BasicDialog
import com.rainccup.aktool.core.designsystem.component.CharPainter
import com.rainccup.aktool.core.designsystem.component.CircleIconButton
import com.rainccup.aktool.core.designsystem.component.EditTextDialog
import com.rainccup.aktool.core.designsystem.icon
import com.rainccup.aktool.core.domain.GameTableQuery
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.Profession
import com.rainccup.aktool.core.platform.ClipboardPort
import com.rainccup.aktool.core.platform.platformUiScale
import com.rainccup.aktool.core.platform.urlEncode
import com.rainccup.aktool.feature.character.viewmodel.CharacterViewModel
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.char_id
import com.rainccup.aktool.resources.character_default_skill_icon
import com.rainccup.aktool.resources.character_elite_0
import com.rainccup.aktool.resources.character_elite_1
import com.rainccup.aktool.resources.character_elite_2
import com.rainccup.aktool.resources.character_elite_bg
import com.rainccup.aktool.resources.character_empty_skill
import com.rainccup.aktool.resources.character_level_bg
import com.rainccup.aktool.resources.character_potential_1
import com.rainccup.aktool.resources.character_potential_2
import com.rainccup.aktool.resources.character_potential_3
import com.rainccup.aktool.resources.character_potential_4
import com.rainccup.aktool.resources.character_potential_5
import com.rainccup.aktool.resources.character_star_mark
import com.rainccup.aktool.resources.character_star_mark_edit
import com.rainccup.aktool.resources.equip_bg
import com.rainccup.aktool.resources.lv
import com.rainccup.aktool.resources.profession_select
import com.rainccup.aktool.resources.search
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.FloatingToolbar
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SearchBar
import kotlin.math.roundToInt

@Composable
fun CharacterPage(
    updateChar: Character?,
    onOpenDetail: (Character) -> Unit,
) {
    val viewModel: CharacterViewModel = koinViewModel()
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
    val baseDensity = LocalDensity.current
    val uiScale = platformUiScale()
    LaunchedEffect(updateChar) {
        updateChar?.let { char ->
            viewModel.updateCharData(char)
        }
    }
    PullToRefreshBox(
        isRefreshing = showLoadAnimate,
        onRefresh = viewModel::initCharData,
        modifier = Modifier.fillMaxSize()
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(baseDensity.density * uiScale, baseDensity.fontScale)
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
                            onOpenDetail(char)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .wrapContentHeight()
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp)
                ) {
                    FloatingToolbar(
                        color = Color.Black.copy(alpha = 0.9f),
                        modifier = Modifier
                            .wrapContentHeight()
                            .offsetPercent(offsetPercentX = professionOffsetX)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 4.dp).clip(RoundedCornerShape(50.dp))
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
                                        .width(48.dp)
                                        .align(alignment = Alignment.CenterEnd)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = if (currentProfession == "ALL") "BACK" else "ALL",
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        autoSize = TextAutoSize.StepBased(),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            items(professions) {
                                Box(
                                    contentAlignment = Alignment.CenterEnd,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Image(
                                        painter = painterResource(it.icon),
                                        contentDescription = it.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .alpha(0.8f)
                                            .height(42.dp)
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
                                            modifier = Modifier.height(42.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    FloatingToolbar(
                        modifier = Modifier.offsetPercent(offsetPercentX = menuOffsetX)
                    ) {
                        Column(modifier = Modifier) {
                            IconButton(
                                onClick = { viewModel.changeSelectState(true) }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = null)
                            }
                            IconButton(onClick = viewModel::changeGainCharState) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                            IconButton(onClick = viewModel::changeSearchState) {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                            IconButton(onClick = viewModel::initCharData) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
        if (splash && !showLoadAnimate) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircleIconButton(
                    icon = Icons.Default.NearMe,
                    size = 72,
                    onClick = viewModel::initCharData,
                    modifier = Modifier
                        .padding(4.dp)
                )
            }
        }
    }
    EditTextDialog(
        title = stringResource(Res.string.char_id),
        show = showGainCharDialog,
        error = { !it.startsWith("char_") || viewModel.existChar(it) },
        onConfirm = { charId ->
            charId?.let {
                coroutineScope.launch {
                    runCatching {
                        viewModel.gainChar(charId)
                    }.onSuccess {
                        viewModel.initCharData()
                    }.onFailure {

                    }
                }
            }
            viewModel.changeGainCharState()
        }
    )
    SearchCharDialog(
        show = showSearchDialog,
        onKeywordType = viewModel::getSearchedCharList,
        onSearchCharId = viewModel::getCharIdByCharName
    ) {
        it?.let { viewModel.searchChar(it) }
        viewModel.changeSearchState()
    }

}

@Composable
fun CharacterCard(
    modifier: Modifier = Modifier,
    char: Character = Character.placeholder(),
    onCharSelect: (Int) -> Unit = { }
) {
    val gameTable: GameTableQuery = koinInject()
    val evolvePhasePainter = when (char.evolvePhase) {
        0 -> Res.drawable.character_elite_0
        1 -> Res.drawable.character_elite_1
        2 -> Res.drawable.character_elite_2
        else -> Res.drawable.character_elite_0
    }
    val charPainter = CharPainter.form(char.rarity!!)
    val profession = Profession.valueOf(char.profession!!)
    Box(
        modifier = modifier
            .height(228.dp)
            .width(108.dp)
            .clipToBounds()
            .clickable { (onCharSelect(char.instId)) }
    ) {
        // ---- card layers: pure Compose layout (no ConstraintLayout) ----
        // gold frame background
        Image(
            painter = painterResource(charPainter.charBgPainter),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 4.dp, y = (-22).dp)
                .size(100.dp, 182.dp)
        )
        // character portrait
        Image(
            painter = portraitPainter(char.skin),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 4.dp, y = 4.dp)
                .width(100.dp)
        )
        // rarity light
        Image(
            painter = painterResource(charPainter.rarityLightPainter),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 4.dp, y = (-22).dp)
                .size(100.dp, 88.dp)
        )
        // upper hub
        Image(
            painter = painterResource(charPainter.upperHubPainter),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 4.dp, y = 4.dp)
                .size(50.dp, 24.dp)
        )
        // profession icon
        Image(
            painterResource(profession.icon), null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 8.dp, y = 8.dp)
                .size(18.dp)
        )
        // rarity stars
        Image(
            painterResource(charPainter.rarityPainter), null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 28.dp, y = 8.dp)
                .height(18.dp)
                .widthIn(max = 74.dp)
        )
        // evolve phase background
        Image(
            painterResource(Res.drawable.character_elite_bg), null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 11.dp, y = (-40).dp)
                .size(27.dp, 48.dp)
                .alpha(0.7f)
        )
        // star mark
        Image(
            painterResource(if (char.starMark == 1) Res.drawable.character_star_mark else Res.drawable.character_star_mark_edit),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 4.dp, y = (-4).dp)
                .height(24.dp)
        )
        // lower hub
        Image(
            painterResource(charPainter.lowerHubPainter), null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(108.dp, 84.dp)
        )
        // evolve phase icon
        Image(
            painterResource(evolvePhasePainter), null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 1.dp, y = (-52).dp)
                .size(48.dp)
        )
        // level badge with LV + level
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 2.dp, y = (-18).dp)
                .size(46.dp)
        ) {
            Image(
                painterResource(Res.drawable.character_level_bg), null,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = stringResource(Res.string.lv),
                color = Color.White,
                style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 6.sp),
                modifier = Modifier.align(Alignment.Center)
                    .offset(y = (-10).dp)
            )
            Text(
                text = char.level.toString(),
                color = Color.White,
                style = TextStyle(fontWeight = FontWeight.Normal, fontSize = 22.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
                    .padding(top = 2.dp)
            )
        }
        // skill icon
        val skill = char.skills.getOrNull(char.defaultSkillIndex)
        val skillPainter = if (skill == null) {
            if (char.currentTmpl == null) {
                painterResource(Res.drawable.character_empty_skill)
            } else {
                val tmplChar = char.tmpl!![char.currentTmpl]
                val tmplSkill = tmplChar?.skills?.getOrNull(tmplChar.defaultSkillIndex)
                if (tmplSkill == null) painterResource(Res.drawable.character_empty_skill)
                else skillPainter(tmplSkill.skillId)
            }
        } else {
            skillPainter(skill.skillId)
        }
        Image(
            painter = skillPainter,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-26).dp)
                .size(26.dp)
        )
        // equip icon, centred between the level badge and the skill icon
        char.currentEquip?.let { equipId ->
            Image(
                painter = equipPainter(gameTable.getEquipType(equipId)),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-31).dp, y = (-22).dp)
                    .size(36.dp)
            )
        }
        // potential rank
        if (char.potentialRank > 0) {
            val potentialPainter = when (char.potentialRank) {
                1 -> Res.drawable.character_potential_1
                2 -> Res.drawable.character_potential_2
                3 -> Res.drawable.character_potential_3
                4 -> Res.drawable.character_potential_4
                else -> Res.drawable.character_potential_5
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-8).dp, y = (-56).dp)
                    .size(16.dp)
            ) {
                Image(painterResource(Res.drawable.equip_bg), null, Modifier.fillMaxSize())
                Image(
                    painterResource(potentialPainter), null,
                    modifier = Modifier.align(Alignment.Center).size(24.dp)
                )
            }
        }
        // character name
        Text(
            char.name!!,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-4).dp)
                .widthIn(max = 100.dp)
        )

    }
}

@Composable
fun SearchCharDialog(
    show: Boolean,
    onKeywordType: (String) -> List<String> = { listOf() },
    onSearchCharId: (String) -> String = { "" },
    onConfirmKeyword: (String?) -> Unit = { }
) {
    val clipboard = koinInject<ClipboardPort>()
    var keyword by remember { mutableStateOf("") }
    val charNameList = remember { mutableStateListOf<String>() }
    var selectedKeyword by remember { mutableStateOf("") }
    BasicDialog(
        show = show,
        title = stringResource(Res.string.search),
        onCancel = { onConfirmKeyword(null) },
        onConfirm = { onConfirmKeyword(selectedKeyword) }
    ) {
        SearchBar(
            inputField = {
                InputField(
                    query = keyword,
                    onQueryChange = {
                        keyword = it
                        charNameList.replace(onKeywordType(keyword))
                    },
                    onSearch = { onConfirmKeyword(selectedKeyword) },
                    expanded = keyword.isNotEmpty(),
                    onExpandedChange = { },
                    label = stringResource(Res.string.search),
                )
            },
            expanded = keyword.isNotEmpty(),
            onExpandedChange = { },
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                charNameList.forEach { word ->
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







