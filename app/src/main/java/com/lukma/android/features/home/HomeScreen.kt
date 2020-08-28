package com.lukma.android.features.home

import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Preview
import androidx.work.WorkInfo
import com.lukma.android.common.UiState
import com.lukma.android.common.WorkerWatcherAmbient
import com.lukma.android.common.ui.GlideImage
import com.lukma.android.common.ui.Shimmer
import com.lukma.android.common.ui.VideoPlayer
import com.lukma.android.domain.post.Post
import com.lukma.android.ui.theme.CleanTheme
import com.lukma.android.worker.UploadPostWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val viewModel = viewModel<HomeViewModel>()
    val postsState by viewModel.posts.observeAsState(initial = UiState.None)

    launchInComposition {
        viewModel.fetchLatestPosts()
    }

    val workerWatcher = WorkerWatcherAmbient.current
    workerWatcher.watch(UploadPostWork.TAG)
    workerWatcher.workInfo?.let { workInfo ->
        val workInfoState by workInfo.observeAsState()
        val isSuccess = workInfoState?.state == WorkInfo.State.SUCCEEDED
        if (isSuccess) {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.fetchLatestPosts()
            }
        }
    }

    LatestPostList(postsState)
}

@Composable
private fun LatestPostList(state: UiState<List<Post>>) {
    when (state) {
        is UiState.Loading -> ScreenLoading()
        is UiState.Success -> PostList(posts = state.data)
    }
}

@Composable
private fun ScreenLoading() {
    Shimmer(modifier = Modifier.fillMaxSize()) {
        val dummy = Post.Image(url = "", author = Post.Author(uid = "", name = ""))
        Column(modifier = Modifier.fillMaxWidth().height(500.dp).padding(8.dp)) {
            PostItem(post = dummy)
        }
    }
}

@Composable
private fun PostList(posts: List<Post>) {
    ConstraintLayout {
        val (postList) = createRefs()
        LazyColumnFor(
            items = posts,
            modifier = Modifier.constrainAs(postList) {
                bottom.linkTo(parent.bottom)
                top.linkTo(parent.top)
                width = Dimension.fillToConstraints
            },
            contentPadding = InnerPadding(8.dp)
        ) {
            if (posts.indexOf(it) == 0) {
                NewPostUploadInfo()
            }
            PostItem(post = it)
            Spacer(modifier = Modifier.preferredHeight(8.dp))
        }
    }
}

@Composable
private fun NewPostUploadInfo() {
    val workerWatcher = WorkerWatcherAmbient.current
    workerWatcher.workInfo?.let { workInfo ->
        val workInfoState by workInfo.observeAsState()
        val isRunning = workInfoState?.state == WorkInfo.State.RUNNING
        if (isRunning) {
            val filePath = workInfoState?.progress?.getString(UploadPostWork.KEY_FILE_PATH)
            val progress = workInfoState?.progress?.getDouble(UploadPostWork.KEY_PROGRESS, 0.0)
                ?.let { it.toFloat() / 100 }

            if (progress != null && progress > 0.0 && progress < 1.0) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                        val (photoImage, progressView) = createRefs()
                        GlideImage(url = filePath ?: "", modifier = Modifier.constrainAs(photoImage) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            width = Dimension.value(50.dp)
                            height = Dimension.value(50.dp)
                        })
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.constrainAs(progressView) {
                                bottom.linkTo(photoImage.bottom)
                                end.linkTo(parent.end)
                                start.linkTo(photoImage.end, margin = 8.dp)
                                top.linkTo(photoImage.top)
                                width = Dimension.fillToConstraints
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        shape = RoundedCornerShape(8.dp)
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (content, authorPhotoImage, authorNameText) = createRefs()

            val contentModifier = Modifier.constrainAs(content) {
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
                top.linkTo(authorNameText.bottom)
                width = Dimension.fillToConstraints
                height = Dimension.value(500.dp)
            }
            when (post) {
                is Post.Image -> GlideImage(
                    url = post.url,
                    modifier = contentModifier,
                    contentScale = ContentScale.Crop
                )
                is Post.Video -> VideoPlayer(
                    url = post.url,
                    modifier = contentModifier
                )
            }
            GlideImage(
                url = post.author.photo ?: "",
                modifier = Modifier.clip(CircleShape).constrainAs(authorPhotoImage) {
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(parent.top, margin = 8.dp)
                    width = Dimension.value(48.dp)
                    height = Dimension.value(48.dp)
                },
                contentScale = ContentScale.Crop
            )
            Text(
                text = post.author.name,
                modifier = Modifier.constrainAs(authorNameText) {
                    bottom.linkTo(authorPhotoImage.bottom)
                    start.linkTo(authorPhotoImage.end, margin = 8.dp)
                    top.linkTo(authorPhotoImage.top)
                },
                maxLines = 1,
                style = typography.subtitle1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CleanTheme {
        HomeScreen()
    }
}
