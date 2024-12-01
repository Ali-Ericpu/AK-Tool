package org.doctorate.aktool.ui.page.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.doctorate.aktool.R

@Preview
@Composable
fun SplashPage() {
    val viewModel: SplashViewModel = viewModel()
    val splash by viewModel.splash.collectAsState()
    AnimatedVisibility(
        splash,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val (logoRef, adRef, skipRef) = remember { createRefs() }
            Image(
                painter = painterResource(R.drawable.launch_logo),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .constrainAs(logoRef) {
                        centerTo(parent)
                    }
            )
            CircleIconButton(
                icon = Icons.Default.Clear,
                onClick = { viewModel.closeSplash() },
                modifier = Modifier.constrainAs(skipRef) {
                    top.linkTo(parent.top, 16.dp)
                    end.linkTo(parent.end, 16.dp)
                }
            )
            Text(
                text = "广告位招租",
                modifier = Modifier.constrainAs(adRef) {
                    top.linkTo(logoRef.bottom, 40.dp)
                    centerHorizontallyTo(parent)
                }
            )
        }
    }
    LaunchedEffect(Unit) {
        delay(2000)
        viewModel.closeSplash()
    }

}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { onClick() },
        modifier = modifier
            .padding(4.dp)
            .size(size.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
//            tint = Color.Black,
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(12.dp)
        )
    }
}