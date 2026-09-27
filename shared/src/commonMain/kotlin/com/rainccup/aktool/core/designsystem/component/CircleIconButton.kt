package com.rainccup.aktool.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CircleIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Int = 40,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .size(size.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(12.dp),
        )
    }
}
