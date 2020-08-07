package com.lukma.android.common.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.lukma.android.R
import java.util.concurrent.TimeUnit

@Composable
fun VideoPlayer(url: String, modifier: Modifier) {
    val context = ContextAmbient.current
    val player = remember {
        SimpleExoPlayer.Builder(context).build().apply {
            val videoSource = Uri.parse(url).toMediaSource(context)
            prepare(videoSource)
        }
    }

    AndroidView(viewBlock = {
        LayoutInflater.from(it).inflate(R.layout.video_player, FrameLayout(context), false)
    }, modifier = modifier) {
        val playerView = it.findViewById<PlayerView>(R.id.playerView)
        playerView.controllerShowTimeoutMs = TimeUnit.SECONDS.toMillis(1).toInt()
        playerView.player = player
    }
}

fun Uri.toMediaSource(context: Context): MediaSource {
    val dataSourceFactory =
        DefaultHttpDataSourceFactory(Util.getUserAgent(context, "appClient"))
    return when (Util.inferContentType(this)) {
        C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(this)
        else -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(this)
    }
}
