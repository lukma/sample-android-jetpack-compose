package com.lukma.android.common.ui

import androidx.compose.foundation.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Snackbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun DelayedSnackbar(
    text: String,
    modifier: Modifier,
    delaying: Long = TimeUnit.SECONDS.toMillis(3),
    shape: Shape = RoundedCornerShape(0.dp)
) {
    var isShow by savedInstanceState { true }

    if (isShow) {
        Snackbar(
            text = { Text(text = text) },
            modifier = modifier,
            shape = shape
        )
    }

    CoroutineScope(Dispatchers.Main).launch {
        delay(delaying)
        isShow = false
    }
}
