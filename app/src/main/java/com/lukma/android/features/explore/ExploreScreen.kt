package com.lukma.android.features.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope.weight
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Preview
import com.lukma.android.common.UiState
import com.lukma.android.common.ui.GridItems
import com.lukma.android.common.ui.Image
import com.lukma.android.common.ui.Shimmer
import com.lukma.android.domain.post.Post
import com.lukma.android.ui.theme.CleanTheme

@Composable
fun ExploreScreen() {
    val viewModel = viewModel<ExploreViewModel>()
    val postsState by viewModel.posts.observeAsState(initial = UiState.None)

    launchInComposition {
        viewModel.fetchRecommendPosts()
    }

    RecommendedPostList(state = postsState)
}

@Composable
private fun RecommendedPostList(state: UiState<List<Post>>) {
    when (state) {
        is UiState.Loading -> ScreenLoading()
        is UiState.Success -> PostList(posts = state.data)
    }
}

@Composable
private fun ScreenLoading() {
    val dummy = Post.Image(url = "", author = Post.Author(uid = "", name = ""))
    Shimmer(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().height(200.dp).padding(8.dp)) {
            PostItem(post = dummy)
            Spacer(modifier = Modifier.preferredWidth(8.dp))
            PostItem(post = dummy)
            Spacer(modifier = Modifier.preferredWidth(8.dp))
            PostItem(post = dummy)
        }
    }
}

@Composable
private fun PostList(posts: List<Post>) {
    ConstraintLayout {
        val (postList) = createRefs()
        GridItems(
            items = posts,
            spanCount = 3,
            modifier = Modifier.constrainAs(postList) {
                width = Dimension.fillToConstraints
            },
            space = 4.dp
        ) {
            PostItem(post = it)
        }
    }
}

@Composable
private fun PostItem(post: Post) {
    Card(modifier = Modifier.aspectRatio(1f).weight(1f)) {
        when (post) {
            is Post.Image -> Image(
                url = post.url,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            is Post.Video -> Image(
                url = post.thumbnail,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CleanTheme {
        ExploreScreen()
    }
}
