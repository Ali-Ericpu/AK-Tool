package com.rainccup.aktool.ui.character

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CharacterPage(
    onOpenDetail: (String) -> Unit,
    viewModel: CharacterViewModel = koinViewModel(),
) {
    val loadAnimate by viewModel.loadAnimate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initCharData()
    }

    Column(Modifier.fillMaxSize()) {
        if (loadAnimate) {
            Text(
                text = "加载中...",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        val characters = viewModel.charList()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(characters, key = { it.instId }) { char ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(char.instId.toString()) },
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = char.name ?: char.charId,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = listOfNotNull(
                                char.profession,
                                char.rank?.let { "★$it" },
                                "Lv.${char.level}",
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
