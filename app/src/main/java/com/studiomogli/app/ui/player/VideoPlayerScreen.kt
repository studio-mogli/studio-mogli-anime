package com.studiomogli.app.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.studiomogli.app.ui.home.HomeViewModel
import okhttp3.OkHttpClient

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val stream by viewModel.currentStream.collectAsState()
    val preferredLang by viewModel.preferredLangCode.collectAsState()

    // Lock landscape & hide system bars for true fullscreen playback
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            activity?.requestedOrientation = originalOrientation
            activity?.window?.let { window ->
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val currentStream = stream ?: return

    val exoPlayer = remember(currentStream.hlsUrl) {
        val headers = mutableMapOf("User-Agent" to "Mozilla/5.0")
        currentStream.referer?.let { headers["Referer"] = it }

        val dataSourceFactory = OkHttpDataSource.Factory(OkHttpClient())
            .setDefaultRequestProperties(headers)

        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setPreferredAudioLanguage(preferredLang)
                    .setPreferredTextLanguage(preferredLang)
            )
        }

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                val subtitleConfigs = currentStream.subtitles.map { sub ->
                    MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.url))
                        .setMimeType(MimeTypes.TEXT_VTT)
                        .setLanguage(sub.languageCode)
                        .setLabel(sub.label)
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                }

                val mediaItem = MediaItem.Builder()
                    .setUri(currentStream.hlsUrl)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .setSubtitleConfigurations(subtitleConfigs)
                    .build()

                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setShowSubtitleButton(true)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
