package com.lukma.android.features.login

import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Preview
import com.lukma.android.R
import com.lukma.android.common.UiState
import com.lukma.android.common.onFailure
import com.lukma.android.common.ui.DelayedSnackbar
import com.lukma.android.ui.NavigationHandlerAmbient
import com.lukma.android.ui.theme.CleanTheme

@Composable
fun LoginScreen() {
    val viewModel = viewModel<LoginViewModel>()
    val authState by viewModel.authResult.observeAsState(initial = UiState.None)

    if (authState is UiState.Success) {
        val navigation = NavigationHandlerAmbient.current
        navigation.checkIsLoggedIn().also { viewModel.clearState() }
    }

    var usernameText by savedInstanceState { "" }
    var passwordText by savedInstanceState { "" }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (usernameInput, passwordInput, signInButton, errorMessage) = createRefs()

        TextField(
            value = usernameText,
            onValueChange = { usernameText = it },
            label = { Text(stringResource(id = R.string.input_username)) },
            modifier = Modifier.constrainAs(usernameInput) {
                end.linkTo(parent.end)
                start.linkTo(parent.start)
                top.linkTo(parent.top, margin = 20.dp)
            },
            keyboardType = KeyboardType.Email
        )
        TextField(
            value = passwordText,
            onValueChange = { passwordText = it },
            label = { Text(stringResource(id = R.string.input_password)) },
            modifier = Modifier.constrainAs(passwordInput) {
                end.linkTo(parent.end)
                start.linkTo(parent.start)
                top.linkTo(usernameInput.bottom, margin = 8.dp)
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password
        )
        Button(
            onClick = {
                viewModel.signIn(usernameText, passwordText)
            },
            modifier = Modifier.constrainAs(signInButton) {
                end.linkTo(passwordInput.end)
                top.linkTo(passwordInput.bottom, margin = 8.dp)
            }
        ) {
            Text(text = stringResource(id = R.string.button_sign_in), style = typography.button)
        }

        if (authState is UiState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                backgroundColor = Color(0x80CCCCCC),
                gravity = ContentGravity.Center
            ) {
                CircularProgressIndicator()
            }
        }

        authState.onFailure {
            val text = it.localizedMessage ?: stringResource(id = R.string.error_default)
            DelayedSnackbar(text = text, modifier = Modifier.constrainAs(errorMessage) {
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CleanTheme {
        LoginScreen()
    }
}
