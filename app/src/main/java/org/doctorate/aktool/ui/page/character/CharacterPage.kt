package org.doctorate.aktool.ui.page.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import org.doctorate.aktool.R
import org.doctorate.aktool.config.Table
import org.doctorate.aktool.pojo.entity.Character
import org.doctorate.aktool.pojo.entity.Skill
import java.net.URLEncoder
import kotlin.math.roundToInt

@Composable
fun CharacterPage(
    viewModel: CharacterViewModel = viewModel()
) {
    val context = LocalContext.current
    val charList = viewModel.charList()
    val splash by viewModel.splash.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    LazyVerticalGrid(
        GridCells.FixedSize(108.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items(charList) { char ->
            CharacterCard(char)
        }
    }
    LaunchedEffect(Unit) {
    }
    if (splash) {
        Box(
            modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (Table.initData(context)) {
                            viewModel.initCharData()
                        }
                    }
                }) {
                Text("GET")
            }
        }
    }
}

@Preview
@Composable
fun CharacterCard(
    char: Character = Character(
        1,
        "char_4080_lin",
        "林",
        "CASTER",
        6,
        25570,
        5,
        7,
        "char_4080_lin#2",
        90,
        0,
        2,
        2,
        1700000000L,
        listOf(
            Skill(
                state = 0,
                skillId = "skchr_lin_1",
                unlock = 1,
                specializeLevel = 3,
                completeUpgradeTime = -1
            ),
            Skill(
                state = 0,
                skillId = "skchr_lin_2",
                unlock = 1,
                specializeLevel = 3,
                completeUpgradeTime = -1
            ),
            Skill(
                state = 0,
                skillId = "skchr_lin_3",
                unlock = 1,
                specializeLevel = 3,
                completeUpgradeTime = -1
            ),
        ),
        "JP",
        null,
        mutableMapOf(),
        0,
        null,
        null
    ),
    onCharChange: (char: Character) -> Unit = { }
) {
    var showDetail by remember { mutableStateOf(false) }
    val evolvePhasePainter = when (char.evolvePhase) {
        0 -> R.drawable.character_elite_0
        1 -> R.drawable.character_elite_1
        2 -> R.drawable.character_elite_2
        else -> R.drawable.character_elite_0
    }
    var rarityPainter = R.drawable.character_star_1
    var charBgPainter = R.drawable.character_charbg_1
    var upperHubPainter = R.drawable.character_upperhub_1
    var lowerHubPainter = R.drawable.character_lowerhub1
    var rarityLightPainter = R.drawable.character_rartylight_1
    val professionPainter = when (char.profession!!) {
        "PIONEER" -> R.drawable.icon_profession_pioneer
        "WARRIOR" -> R.drawable.icon_profession_warrior
        "SNIPER" -> R.drawable.icon_profession_sniper
        "SPECIAL" -> R.drawable.icon_profession_special
        "TANK" -> R.drawable.icon_profession_tank
        "CASTER" -> R.drawable.icon_profession_caster
        "MEDIC" -> R.drawable.icon_profession_medic
        "SUPPORT" -> R.drawable.icon_profession_support
        else -> throw IllegalArgumentException("unknown character profession :${char.profession}")
    }
    when (char.rank!!) {
        2 -> {
            rarityPainter = R.drawable.character_star_2
            upperHubPainter = R.drawable.character_upperhub_2
            rarityLightPainter = R.drawable.character_rartylight_2
        }

        3 -> {
            rarityPainter = R.drawable.character_star_3
            upperHubPainter = R.drawable.character_upperhub_3
            rarityLightPainter = R.drawable.character_rartylight_3
        }

        4 -> {
            rarityPainter = R.drawable.character_star_4
            charBgPainter = R.drawable.character_charbg_4
            upperHubPainter = R.drawable.character_upperhub_4
            lowerHubPainter = R.drawable.character_lowerhub4
            rarityLightPainter = R.drawable.character_rartylight_4
        }

        5 -> {
            rarityPainter = R.drawable.character_star_5
            charBgPainter = R.drawable.character_charbg_5
            upperHubPainter = R.drawable.character_upperhub_5
            lowerHubPainter = R.drawable.character_lowerhub5
            rarityLightPainter = R.drawable.character_rartylight_5
        }

        6 -> {
            rarityPainter = R.drawable.character_star_6
            charBgPainter = R.drawable.character_charbg_6
            upperHubPainter = R.drawable.character_upperhub_6
            lowerHubPainter = R.drawable.character_lowerhub6
            rarityLightPainter = R.drawable.character_rartylight_6
        }
    }
    Box(
        modifier = Modifier
            .height(224.dp)
            .width(108.dp)
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (charBgRef, portraitRef, topHubRef, bottomHubRef, charNameRef, starMarkRef, rarityLightRef, professionRef) = remember { createRefs() }
            val (potentialBgRef, starRef, evoRef, evoBgRef, levelRef, lvRef, levelBgRef, skillRef, potentialRef) = remember { createRefs() }
            //char bg
            Image(
                painter = painterResource(charBgPainter),
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
            val portraitId = URLEncoder.encode(Table.getSkinPortraitId(char.skin), "UTF-8")
//            val portraitId = "23123"
            val link = "https://torappu.prts.wiki/assets/char_portrait/$portraitId.png"
            Image(
                painter = rememberAsyncImagePainter(link),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .fillMaxWidth()
                    .constrainAs(portraitRef) {
                        top.linkTo(topHubRef.top)
//                        bottom.linkTo(charBgRef.bottom)
                    }
            )
            //rarity light
            Image(
                painter = painterResource(rarityLightPainter),
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
                painter = painterResource(upperHubPainter),
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
                painterResource(professionPainter), null,
                modifier = Modifier
                    .size(18.dp)
                    .constrainAs(professionRef) {
                        start.linkTo(topHubRef.start, 4.dp)
                        top.linkTo(topHubRef.top, 4.dp)
                    }
            )
            //char rarity star
            Image(
                painterResource(rarityPainter), null,
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
                painterResource(R.drawable.character_elite_bg), null,
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
            val starMarkPainter = if (char.starMark == 1) R.drawable.character_star_mark
            else R.drawable.character_star_mark_edit
            Image(
                painterResource(starMarkPainter),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(24.dp)
                    .constrainAs(starMarkRef) {
                        start.linkTo(parent.start, 4.dp)
                        bottom.linkTo(charNameRef.bottom)
                    }
            )
            //lower hub
            Image(
                painterResource(lowerHubPainter), null,
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
                painterResource(R.drawable.character_level_bg), null,
                modifier = Modifier
                    .size(46.dp)
                    .constrainAs(levelBgRef) {
                        start.linkTo(parent.start, 2.dp)
                        bottom.linkTo(starMarkRef.top, (-8).dp)
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
                text = stringResource(R.string.lv),
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
                    painterResource(R.drawable.character_empty_skill)
                } else {
                    val tmplChar = char.tmpl!![char.currentTmpl]
                    val tmplSkill = tmplChar?.skills?.getOrNull(tmplChar.defaultSkillIndex)
                    if (tmplSkill == null) {
                        painterResource(R.drawable.character_empty_skill)
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
                    1 -> R.drawable.character_potential_1
                    2 -> R.drawable.character_potential_2
                    3 -> R.drawable.character_potential_3
                    4 -> R.drawable.character_potential_4
                    else -> R.drawable.character_potential_5
                }
                Image(
                    painterResource(R.drawable.equip_bg), null,
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
    if (showDetail) {
        CharacterDetail(char.copy()) {
            showDetail = false
            it?.let { onCharChange(it) }
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
fun skillPainter(skillId: String): Painter {
    val skillId = URLEncoder.encode(skillId, "UTF-8")
    val imageUrl =
        "https://raw.githubusercontent.com/yuanyan3060/ArknightsGameResource/refs/heads/main/skill/skill_icon_$skillId.png"
    return rememberAsyncImagePainter(
        model = imageUrl,
        placeholder = painterResource(R.drawable.character_default_skill_icon),
        error = painterResource(R.drawable.character_default_skill_icon),
    )
}