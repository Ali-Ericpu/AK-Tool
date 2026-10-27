package com.rainccup.aktool.feature.character.ui


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import com.rainccup.aktool.core.common.LocalImageConfig
import com.rainccup.aktool.core.common.replace
import com.rainccup.aktool.core.designsystem.LocalFloatingBarInset
import com.rainccup.aktool.core.designsystem.anim.itemEnterAnimation
import com.rainccup.aktool.core.designsystem.component.BasicDialog
import com.rainccup.aktool.core.designsystem.component.CharPainter
import com.rainccup.aktool.core.designsystem.component.CircleIconButton
import com.rainccup.aktool.core.designsystem.icon
import com.rainccup.aktool.core.domain.GameTableQuery
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.model.Profession
import com.rainccup.aktool.core.navigation.characterCardSharedElement
import com.rainccup.aktool.core.platform.platformUiScale
import com.rainccup.aktool.core.platform.urlEncode
import com.rainccup.aktool.feature.character.viewmodel.CharacterViewModel
import com.rainccup.aktool.resources.Res
import com.rainccup.aktool.resources.cd_back_to_top
import com.rainccup.aktool.resources.cd_menu
import com.rainccup.aktool.resources.cd_refresh
import com.rainccup.aktool.resources.char_id
import com.rainccup.aktool.resources.character_default_skill_icon
import com.rainccup.aktool.resources.character_elite_0
import com.rainccup.aktool.resources.character_elite_1
import com.rainccup.aktool.resources.character_elite_2
import com.rainccup.aktool.resources.character_elite_bg
import com.rainccup.aktool.resources.character_empty_skill
import com.rainccup.aktool.resources.character_level_bg
import com.rainccup.aktool.resources.character_not_exist
import com.rainccup.aktool.resources.character_potential_1
import com.rainccup.aktool.resources.character_potential_2
import com.rainccup.aktool.resources.character_potential_3
import com.rainccup.aktool.resources.character_potential_4
import com.rainccup.aktool.resources.character_potential_5
import com.rainccup.aktool.resources.character_profession_all
import com.rainccup.aktool.resources.character_profession_back
import com.rainccup.aktool.resources.character_star_mark
import com.rainccup.aktool.resources.character_star_mark_edit
import com.rainccup.aktool.resources.equip_bg
import com.rainccup.aktool.resources.longpress_to_copy
import com.rainccup.aktool.resources.lv
import com.rainccup.aktool.resources.search
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.FloatingToolbar
import top.yukonga.miuix.kmp.basic.FloatingToolbarDefaults
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun CharacterPage(
    onOpenDetail: (Character) -> Unit,
) {
    val viewModel: CharacterViewModel = koinViewModel()
    val splash by viewModel.splash.collectAsState()
    val showLoadAnimate by viewModel.loadAnimate.collectAsStateWithLifecycle()
    val currentProfession by viewModel.profession.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val selectProfession by viewModel.isSelect.collectAsStateWithLifecycle()
    val lazyGridState = rememberLazyGridState()
    var lastProfession by rememberSaveable { mutableStateOf(currentProfession) }
    LaunchedEffect(currentProfession) {
        if (lastProfession != currentProfession) {
            lastProfession = currentProfession
            lazyGridState.animateScrollToItem(0)
        }
    }
    val baseDensity = LocalDensity.current
    val uiScale = platformUiScale()
    PullToRefreshBox(
        isRefreshing = showLoadAnimate,
        onRefresh = viewModel::initCharData,
        modifier = Modifier.fillMaxSize()
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(baseDensity.density * uiScale, baseDensity.fontScale)
        ) {
            if (!splash) {
                LazyVerticalGrid(
                    GridCells.FixedSize(CharacterGridMetrics.CardWidth),
                    verticalArrangement = Arrangement.Top,
                    horizontalArrangement = Arrangement.SpaceAround,
                    state = lazyGridState,
                    // 内容直接在悬浮底栏底下滑过去，最后一排靠这段底部留白停在浮层之上。
                    contentPadding = PaddingValues(bottom = LocalFloatingBarInset.current),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(characters, key = { _, char -> char.instId }) { index, char ->
                        CharacterCard(
                            modifier = Modifier
                                .characterCardSharedElement(char.instId)
                                .itemEnterAnimation(index),
                            char = char,
                        ) {
                            onOpenDetail(char)
                        }
                    }
                }
                CharacterToolbars(
                    viewModel = viewModel,
                    lazyGridState = lazyGridState,
                    currentProfession = currentProfession,
                    professionOpen = selectProfession,
                )
            }
        }
        if (splash && !showLoadAnimate) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircleIconButton(
                    icon = Icons.Default.NearMe,
                    size = 68,
                    onClick = viewModel::initCharData,
                    modifier = Modifier
                        .padding(4.dp)
                )
            }
        }
    }

}

@Composable
fun CharacterCard(
    modifier: Modifier = Modifier,
    char: Character = Character.placeholder(),
    onCharSelect: (Int) -> Unit = { }
) {
    val gameTable: GameTableQuery = koinInject()
    val rarity = char.rarity
    val profession = char.profession?.let { runCatching { Profession.valueOf(it) }.getOrNull() }
    val evolvePhasePainter = when (char.evolvePhase) {
        0 -> Res.drawable.character_elite_0
        1 -> Res.drawable.character_elite_1
        2 -> Res.drawable.character_elite_2
        else -> Res.drawable.character_elite_0
    }
    val charPainter = CharPainter.form(rarity ?: 1)
    Box(
        modifier = modifier
            .height(CharacterGridMetrics.CardHeight)
            .width(CharacterGridMetrics.CardWidth)
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
        // profession icon（缺 profession 时不渲染）
        if (profession != null) {
            Image(
                painterResource(profession.icon), null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 8.dp, y = 8.dp)
                    .size(18.dp)
            )
        }
        // rarity stars
        Image(
            painterResource(charPainter.rarityPainter), null,
            contentScale = ContentScale.Crop,
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
        // skill icon：模板技能可能缺失，这里逐层安全取值
        val skill = char.skills.getOrNull(char.defaultSkillIndex)
        val templateSkill = char.currentTmpl
            ?.let { templateId -> char.tmpl?.get(templateId) }
            ?.let { template -> template.skills.getOrNull(template.defaultSkillIndex) }
        val skillPainter = when {
            skill != null -> skillPainter(skill.skillId)
            templateSkill != null -> skillPainter(templateSkill.skillId)
            else -> painterResource(Res.drawable.character_empty_skill)
        }
        Image(
            painter = skillPainter,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-26).dp)
                .size(26.dp)
        )
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
    onKeywordType: (String) -> List<String> = { emptyList() },
    onPickName: (String) -> Unit = { },
    onCopyCharId: (String) -> Unit = { },
    onConfirmKeyword: (String?) -> Unit = { }
) {
    var show by remember { mutableStateOf(false) }
    var keyword by remember(show) { mutableStateOf("") }
    var selectedKeyword by remember(show) { mutableStateOf("") }
    val charNameList = remember(show) { mutableStateListOf<String>() }

    // 关键词变化时刷新建议；查询用刚输入的新值。
    LaunchedEffect(keyword) {
        charNameList.replace(onKeywordType(keyword))
    }

    IconButton(onClick = { show = true }) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onBackground
        )
    }
    BasicDialog(
        show = show,
        title = stringResource(Res.string.search),
        summary = stringResource(Res.string.longpress_to_copy).takeIf { charNameList.isNotEmpty() },
        onCancel = { show = false },
        onConfirm = {
            if (keyword.isNotBlank()) {
                onConfirmKeyword(keyword)
            }
            show = false
        }
    ) {
        SearchBar(
            inputField = {
                InputField(
                    query = keyword,
                    onQueryChange = { keyword = it },
                    onSearch = {
                        if (keyword.isNotBlank()) {
                            onConfirmKeyword(keyword)
                        }
                        show = false
                    },
                    expanded = keyword.isNotEmpty(),
                    onExpandedChange = { },
                    label = stringResource(Res.string.search),
                )
            },
            expanded = keyword.isNotEmpty(),
            onExpandedChange = { },
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                charNameList.forEach { word ->
                    val selected = selectedKeyword == word
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) MiuixTheme.colorScheme.primary
                                else Color.LightGray
                            )
                            .combinedClickable(
                                enabled = true,
                                onClick = {
                                    selectedKeyword = word
                                    onPickName(word)
                                    show = false
                                },
                                onLongClick = { onCopyCharId(word) }
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

@Composable
fun GainCharDialog(
    onGainChar: (String) -> Unit,
    checkExist: (String) -> Boolean,
) {
    var show by remember { mutableStateOf(false) }
    var charId by remember(show) { mutableStateOf("") }
    var error by remember(charId) { mutableStateOf(!checkExist(charId)) }
    IconButton(onClick = { show = true }) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onBackground
        )
    }
    BasicDialog(
        title = stringResource(Res.string.char_id),
        show = show,
        error = error,
        summary = if (error) stringResource(Res.string.character_not_exist) else "",
        onConfirm = { onGainChar(charId) },
        onCancel = { show = false }
    ) {
        TextField(value = charId, onValueChange = { charId = it })
    }
}


@Composable
fun portraitPainter(skinId: String): Painter {
    val encode = urlEncode(skinId)
    return rememberAsyncImagePainter(model = LocalImageConfig.current.portrait + encode + ".png")
}

@Composable
fun skillPainter(skillId: String): Painter {
    val encode = urlEncode(skillId)
    return rememberAsyncImagePainter(
        model = LocalImageConfig.current.skill + encode + ".png",
        placeholder = painterResource(Res.drawable.character_default_skill_icon),
        error = painterResource(Res.drawable.character_default_skill_icon),
    )
}

@Composable
fun equipPainter(equipId: String): Painter {
    return rememberAsyncImagePainter(model = LocalImageConfig.current.equip + equipId + ".png")
}


/* ---------------- 右侧悬浮工具栏 ---------------- */
private val ToolbarCornerRadius = 28.dp
private val ToolbarOutsidePadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
private val ToolbarIconSize = 40.dp

/**
 * 右侧两个悬浮面板：职业筛选与页内菜单。
 *
 * 两者共用同一套外观（surfaceContainer 底 + 28dp 圆角 + 不要投影），但同一时刻只组合一个：
 * 收起的那一个不进 composition，就没有"已经滑出去却仍然露着一个角"的可能。
 */
@Composable
private fun BoxScope.CharacterToolbars(
    viewModel: CharacterViewModel,
    lazyGridState: LazyGridState,
    currentProfession: String,
    professionOpen: Boolean,
) {
    AnimatedContent(
        targetState = professionOpen,
        transitionSpec = {
            val spec = tween<IntOffset>(300)
            slideInHorizontally(animationSpec = spec) { it } togetherWith
                    slideOutHorizontally(animationSpec = spec) { it }
        },
        label = "characterToolbars",
        modifier = Modifier.align(Alignment.CenterEnd)
    ) { open ->
        if (open) {
            ProfessionPanel(
                currentProfession = currentProfession,
                onSelectProfession = viewModel::selectProfession,
                onCollapse = { viewModel.changeSelectState(false) },
            )
        } else {
            CharacterMenuPanel(
                viewModel = viewModel,
                lazyGridState = lazyGridState,
            )
        }
    }
}

/** 职业筛选面板：顶部是「全部 / 收起」，下面是八个职业图标。 */
@Composable
private fun ProfessionPanel(
    currentProfession: String,
    onSelectProfession: (String) -> Unit,
    onCollapse: () -> Unit,
) {
    FloatingToolbar(
        color = Color.Transparent,
        outSidePadding = ToolbarOutsidePadding,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(ToolbarCornerRadius))
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(ToolbarIconSize)
                    .clickable {
                        if (currentProfession == "ALL") onCollapse() else onSelectProfession("ALL")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (currentProfession == "ALL") {
                            Res.string.character_profession_back
                        } else {
                            Res.string.character_profession_all
                        }
                    ),
                    color = if (currentProfession == "ALL") {
                        MiuixTheme.colorScheme.primary
                    } else {
                        Color.White
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            Profession.entries.forEach { profession ->
                ProfessionIconButton(
                    profession = profession,
                    selected = currentProfession == profession.name,
                    onClick = { onSelectProfession(profession.name) },
                )
            }
        }
    }
}

/** 单个职业图标：黑色底映射成透明，选中时再把剩下的白色染成主题主色。 */
@Composable
private fun ProfessionIconButton(
    profession: Profession,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val primary = MiuixTheme.colorScheme.primary
    val selectedFilter = remember(primary) { professionIconFilter(primary) }
    val plainFilter = remember { professionIconFilter(null) }
    Box(
        modifier = Modifier
            .size(ToolbarIconSize)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(profession.icon),
            contentDescription = profession.name,
            contentScale = ContentScale.Crop,
            alpha = 0.8f,
            colorFilter = if (selected) selectedFilter else plainFilter,
            modifier = Modifier.padding(2.dp).fillMaxSize()
        )
    }
}

/**
 * 职业图标的颜色矩阵。
 *
 * 1. 黑→透明：alpha 取原图灰度（`A' = R`）——黑底 alpha 归零、白图形 alpha 满值，
 *    抗锯齿灰阶自动变成半透明过渡，既不留黑边也不会发硬。
 * 2. 传入 [tint] 时再把剩下的白色按各通道缩放（`R' = tint.r · R` 等）染成该色。
 */
private fun professionIconFilter(tint: Color?): ColorFilter {
    val values = if (tint == null) {
        floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            1f, 0f, 0f, 0f, 0f,
        )
    } else {
        floatArrayOf(
            tint.red, 0f, 0f, 0f, 0f,
            tint.green, 0f, 0f, 0f, 0f,
            tint.blue, 0f, 0f, 0f, 0f,
            1f, 0f, 0f, 0f, 0f,
        )
    }
    return ColorFilter.colorMatrix(ColorMatrix(values))
}

/** 页内菜单面板：展开职业面板、新增 / 搜索角色、刷新、回到顶部。 */
@Composable
private fun CharacterMenuPanel(
    viewModel: CharacterViewModel,
    lazyGridState: LazyGridState,
) {
    val coroutineScope = rememberCoroutineScope()
    FloatingToolbar(
        color = FloatingToolbarDefaults.defaultColor().copy(alpha = 0.9f),
        cornerRadius = ToolbarCornerRadius,
        outSidePadding = ToolbarOutsidePadding,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = { viewModel.changeSelectState(true) }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(Res.string.cd_menu),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
            GainCharDialog(onGainChar = viewModel::gainChar) { charId ->
                viewModel.existChar(charId)
            }
            SearchCharDialog(
                onKeywordType = viewModel::getSearchedCharList,
                onPickName = viewModel::pickName,
                onCopyCharId = viewModel::copyCharId,
                onConfirmKeyword = viewModel::searchChar,
            )
            IconButton(onClick = viewModel::initCharData) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.cd_refresh),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
            IconButton(
                onClick = {
                    coroutineScope.launch { lazyGridState.animateScrollToItem(0) }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = stringResource(Res.string.cd_back_to_top),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
