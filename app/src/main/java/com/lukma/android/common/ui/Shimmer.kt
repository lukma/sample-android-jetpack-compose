package com.lukma.android.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.viewinterop.AndroidView
import com.facebook.shimmer.ShimmerFrameLayout

@Composable
fun Shimmer(modifier: Modifier, content: @Composable () -> Unit) {
    val context = ContextAmbient.current
    val shimmerWrapper = remember {
        ShimmerFrameLayout(context).apply {
            setContent(Recomposer.current()) {
                content()
            }
        }
    }

    launchInComposition {
        shimmerWrapper.startShimmer()
    }

    AndroidView(viewBlock = { shimmerWrapper }, modifier = modifier)
}
