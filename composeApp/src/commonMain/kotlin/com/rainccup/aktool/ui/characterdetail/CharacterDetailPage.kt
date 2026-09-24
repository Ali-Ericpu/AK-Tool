package com.rainccup.aktool.ui.characterdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.rainccup.aktool.core.model.Character
import com.rainccup.aktool.core.platform.Messenger
import com.rainccup.aktool.ui.character.CharacterViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CharacterDetailPage(
    charInstId: String,
    onSaved: () -> Unit,
    vm: CharacterDetailViewModel = koinViewModel { parametersOf(charInstId) },
    charViewModel: CharacterViewModel = koinViewModel(),
    messenger: Messenger = koinInject(),
) {
    val char by vm.char.collectAsState()
    val maxLevel by vm.maxLevel.collectAsState()
    val maxEvoPhase by vm.maxEvoPhase.collectAsState()

    LaunchedEffect(charInstId) {
        charViewModel.characterData[charInstId]?.let {
            vm.accept(it)
            vm.updateMaxEvoPhase(it.charId)
            vm.updateMaxLevel(it.charId, it.evolvePhase)
            vm.updateMaxSkillLevel(it.evolvePhase)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = char.name ?: char.charId,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text("等级: ${char.level} / $maxLevel")
        Slider(
            value = char.level.coerceIn(1, maxLevel).toFloat(),
            onValueChange = { level ->
                val next = level.toInt().coerceIn(1, maxLevel)
                vm.accept(char.copy(level = next))
            },
            valueRange = 1f..maxLevel.toFloat().coerceAtLeast(1f),
        )
        Text("精英阶段: ${char.evolvePhase} / $maxEvoPhase")
        Slider(
            value = char.evolvePhase.coerceIn(0, maxEvoPhase).toFloat(),
            onValueChange = { phase ->
                vm.changeEvoPhase(phase.toInt().coerceIn(0, maxEvoPhase))
            },
            valueRange = 0f..maxEvoPhase.toFloat().coerceAtLeast(0f),
        )
        Button(
            onClick = {
                charViewModel.viewModelScope.launch {
                    try {
                        charViewModel.changeCharData(char)
                        onSaved()
                    } catch (e: Exception) {
                        messenger.show(e.message ?: "error")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("保存")
        }
    }
}
