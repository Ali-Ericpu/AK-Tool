package org.doctorate.aktool

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.doctorate.aktool.ui.page.RoutePage
import org.doctorate.aktool.ui.theme.AKToolTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT > 28) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            AKToolTheme {
                RoutePage()
            }
        }
    }
}

