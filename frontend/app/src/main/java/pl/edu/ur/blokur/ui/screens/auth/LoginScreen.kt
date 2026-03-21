package pl.edu.ur.blokur.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.R
import pl.edu.ur.blokur.ui.components.BlokurCard
import pl.edu.ur.blokur.ui.components.BlokurPrimaryButton
import pl.edu.ur.blokur.ui.components.BlokurTextField
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme
import pl.edu.ur.blokur.ui.theme.Indigo50
import pl.edu.ur.blokur.ui.theme.NeutralBg

@Composable
fun LoginScreen(onLoginClick: (email: String, password: String) -> Unit = { _, _ -> }) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(NeutralBg),
    ) {
        // Dekoracyjny gradient w górnej części ekranu
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Indigo50, NeutralBg),
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Logo / nagłówek
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            BlokurCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Pole e-mail
                BlokurTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.login_email_label),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                        )
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pole hasła
                BlokurTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.login_password_label),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation =
                        if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector =
                                    if (passwordVisible) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                contentDescription =
                                    if (passwordVisible) {
                                        stringResource(R.string.login_hide_password)
                                    } else {
                                        stringResource(R.string.login_show_password)
                                    },
                            )
                        }
                    },
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Przycisk logowania
                BlokurPrimaryButton(
                    text = stringResource(R.string.login_button),
                    onClick = { onLoginClick(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank(),
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Login – Light",
)
@Composable
private fun LoginScreenPreviewLight() {
    BlokurPreviewTheme(darkTheme = false) {
        LoginScreen()
    }
}

@Composable
private fun LoginScreenPreviewDark() {
    BlokurPreviewTheme(darkTheme = true) {
        LoginScreen()
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Login – Filled",
)
@Composable
private fun LoginScreenPreviewFilled() {
    BlokurPreviewTheme(darkTheme = false) {
        // LoginScreen zarządza własnym stanem – preview pokazuje ekran z domyślnymi wartościami
        LoginScreen()
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Login – Large Font",
    fontScale = 1.5f,
)
@Composable
private fun LoginScreenPreviewLargeFont() {
    BlokurPreviewTheme(darkTheme = false) {
        LoginScreen()
    }
}
