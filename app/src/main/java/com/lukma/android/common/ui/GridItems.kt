package com.lukma.android.common.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun <T> GridItems(
    items: List<T>,
    spanCount: Int,
    modifier: Modifier,
    space: Dp,
    childs: @Composable (T) -> Unit
) {
    val rows = mutableListOf<List<T>>()
    for (i in items.indices step spanCount) {
        val toIndex = if (i + spanCount < items.size) i + spanCount else items.size
        rows.add(items.subList(i, toIndex))
    }

    LazyColumnFor(
        items = rows,
        modifier = modifier,
        contentPadding = InnerPadding(space)
    ) { row ->
        Row {
            row.forEachIndexed { index, item ->
                childs(item)
                if (index < spanCount - 1) {
                    Spacer(Modifier.preferredWidth(space))
                }
            }
            if (row.size < spanCount) {
                for (i in 0 until spanCount - row.size) {
                    Spacer(Modifier.weight(1f, true))
                }
            }
        }
        Spacer(Modifier.preferredHeight(space))
    }
}
