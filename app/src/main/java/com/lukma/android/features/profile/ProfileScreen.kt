package com.lukma.android.features.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.ClickableText
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.core.content.FileProvider
import androidx.ui.tooling.preview.Preview
import com.lukma.android.BuildConfig
import com.lukma.android.R
import com.lukma.android.common.*
import com.lukma.android.common.ui.Image
import com.lukma.android.ui.NavigationHandlerAmbient
import com.lukma.android.ui.theme.CleanTheme

@Composable
fun ProfileScreen() {
    val context = ContextAmbient.current
    val permission = PermissionUtilsAmbient.current
    val activityResult = ActivityResultHandlerAmbient.current

    val viewModel = viewModel<ProfileViewModel>()
    val myProfileState by viewModel.myProfile.observeAsState()
    val signOutState by viewModel.signOutResult.observeAsState()

    if (signOutState is UiState.Success) {
        val navigation = NavigationHandlerAmbient.current
        navigation.checkIsLoggedIn().also { viewModel.clearState() }
    }

    launchInComposition {
        viewModel.fetchMyProfile()
    }

    ConstraintLayout(Modifier.fillMaxSize()) {
        if (myProfileState != null) {
            val (topAppBar, photoImage, displayNameText) = createRefs()

            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.screen_profile_title))
                },
                modifier = Modifier.constrainAs(topAppBar) {
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                },
                actions = {
                    ClickableText(
                        text = AnnotatedString(stringResource(id = R.string.button_sign_out)),
                        onClick = {
                            viewModel.signOut()
                        }
                    )
                }
            )
            Image(
                url = myProfileState?.photo ?: "",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = {
                        showSelectImageDialog(
                            context = context,
                            permission = permission,
                            activityResult = activityResult,
                            onResultOk = {
                                viewModel.updateMyProfile(photo = it)
                            }
                        )
                    })
                    .constrainAs(photoImage) {
                        end.linkTo(parent.end)
                        start.linkTo(parent.start)
                        top.linkTo(topAppBar.bottom, margin = 16.dp)
                        width = Dimension.value(100.dp)
                        height = Dimension.value(100.dp)
                    },
                contentScale = ContentScale.Crop
            )
            Text(
                text = myProfileState?.displayName ?: "",
                modifier = Modifier.constrainAs(displayNameText) {
                    end.linkTo(parent.end, margin = 8.dp)
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(photoImage.bottom, margin = 8.dp)
                },
                maxLines = 1,
                style = MaterialTheme.typography.h6
            )
        }
    }
}

private fun showSelectImageDialog(
    context: Context,
    permission: PermissionUtils,
    activityResult: ActivityResultHandler,
    onResultOk: (String) -> Unit
) {
    val options = ArrayAdapter<String>(context, android.R.layout.select_dialog_item).apply {
        add(context.getString(R.string.label_pick_gallery))
        add(context.getString(R.string.label_capture_photo))
    }

    AlertDialog.Builder(context)
        .setAdapter(options) { _, which ->
            when (which) {
                0 -> permission.runWithPermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    val intent = Intent().apply {
                        type = "image/*"
                        action = Intent.ACTION_GET_CONTENT
                    }
                    activityResult.launch(intent) {
                        val path = it?.data?.let { data -> getPathFromUri(context, data) }
                        path?.run(onResultOk)
                    }
                }
                1 -> {
                    val outputDirectory = getOutputDirectory(context)
                    val outputFile = createFile(outputDirectory, ".jpg")
                    val authority = "${BuildConfig.APPLICATION_ID}.provider"
                    val outputUri = FileProvider.getUriForFile(context, authority, outputFile)
                    val intent = Intent().apply {
                        action = MediaStore.ACTION_IMAGE_CAPTURE
                        putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
                    }
                    activityResult.launch(intent) {
                        onResultOk(outputFile.toString())
                    }
                }
            }
        }
        .show()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CleanTheme {
        ProfileScreen()
    }
}
