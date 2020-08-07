package com.lukma.android.common.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ContextAmbient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lukma.android.R
import com.lukma.android.common.UiState

@Composable
fun Image(
    url: String,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    onLoading: @Composable () -> Unit = { DefaultImageUrlLoading(modifier) },
    onFailure: @Composable () -> Unit = { DefaultImageUrlError(modifier) }
) {
    when (val state = loadImageUrl(url)) {
        is UiState.Loading -> onLoading()
        is UiState.Success -> androidx.compose.foundation.Image(
            asset = state.data.asImageAsset(),
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )
        is UiState.Failure -> onFailure()
    }
}

@Composable
fun DefaultImageUrlLoading(modifier: Modifier) {
    LoopAnimation(raw = R.raw.image_loading, modifier = modifier)
}

@Composable
fun DefaultImageUrlError(modifier: Modifier) {
    Box(
        modifier = modifier,
        backgroundColor = Color.Gray
    )
}

@Composable
fun loadImageUrl(url: String): UiState<Bitmap> {
    var bitmapState: UiState<Bitmap> by state { UiState.Loading }

    Glide.with(ContextAmbient.current)
        .asBitmap()
        .load(url)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                bitmapState = UiState.Success(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onLoadFailed(errorDrawable: Drawable?) {
                val error = Exception("Fail to load image")
                bitmapState = UiState.Failure(error)
            }
        })

    return bitmapState
}
