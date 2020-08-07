package com.lukma.android.common.ui

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

@Composable
fun LoopAnimation(@RawRes raw: Int, modifier: Modifier) {
    val context = ContextAmbient.current
    val loadingView = remember {
        LottieAnimationView(context).apply {
            setAnimation(raw)
            repeatCount = LottieDrawable.INFINITE
            repeatMode = LottieDrawable.RESTART
        }
    }

    launchInComposition {
        loadingView.playAnimation()
    }

    ConstraintLayout(modifier = modifier) {
        val (loading) = createRefs()
        AndroidView(viewBlock = { loadingView }, modifier = Modifier.constrainAs(loading) {
            bottom.linkTo(parent.bottom)
            end.linkTo(parent.end)
            start.linkTo(parent.start)
            top.linkTo(parent.top)
        })
    }
}
