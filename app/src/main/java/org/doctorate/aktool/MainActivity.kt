package org.doctorate.aktool

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.rememberAsyncImagePainter
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.util.DebugLogger
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import org.doctorate.aktool.config.AppConfigContext
import org.doctorate.aktool.config.LocalAppConfig
import org.doctorate.aktool.config.readConfig
import org.doctorate.aktool.config.writeConfig
import org.doctorate.aktool.ui.page.RoutePage
import org.doctorate.aktool.ui.page.splash.SplashPage
import org.doctorate.aktool.ui.theme.AKToolTheme
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity(), SingletonImageLoader.Factory {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT > 28) {
            window.isNavigationBarContrastEnforced = false
        }
        val context = this
        setContent {
            var config by remember { mutableStateOf(readConfig(this)) }
            val coroutineScope = rememberCoroutineScope()
            CompositionLocalProvider(LocalAppConfig provides AppConfigContext(config) { new ->
                coroutineScope.launch {
                    config = new
                    writeConfig(
                        newConfig = new,
                        context = context,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                R.string.save_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onFailure = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                    )
                }
            }) {
                AKToolTheme(
                    darkTheme = config.darkMode,
                    dynamicColor = config.dynamicColor
                ) {
                    Surface {
                        if (config.bgPicUri.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(config.bgPicUri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxHeight()
                            )
                        }
                        RoutePage()
                        SplashPage()
                    }
                }
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.1)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizePercent(0.02)
                    .build()
            }
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            OkHttpClient.Builder()
                                .readTimeout(30, TimeUnit.SECONDS)
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .callTimeout(30, TimeUnit.SECONDS)
                                .build()
                        }
                    )
                )
            }
            .logger(DebugLogger())
            .build()
    }
}

