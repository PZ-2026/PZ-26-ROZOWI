package pl.edu.ur.blokur.ui.views.auth.contents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.PrimaryButton
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.views.auth.utils.ResetPasswordFormFields
import pl.edu.ur.blokur.ui.views.auth.utils.ResetPasswordState

@Composable
fun ResetPasswordForm(
    state: ResetPasswordState,
    formFields: ResetPasswordFormFields,
    onFormChanged: (ResetPasswordFormFields) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isLoading = state is ResetPasswordState.Loading
    val isSuccess = state is ResetPasswordState.Success
    val isTokenExpired = state is ResetPasswordState.TokenExpired

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ustaw nowe hasło",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Wprowadź nowe hasło do swojego konta. Hasło musi mieć co najmniej 8 znaków.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AnimatedVisibility(
            visible = isSuccess,
            enter = fadeIn() + slideInVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SuccessGreen.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = (state as? ResetPasswordState.Success)?.message ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuccessGreen,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (isTokenExpired) {
            Text(
                text = (state as ResetPasswordState.TokenExpired).message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "Poproś o nowy link",
                onClick = onNavigateToForgotPassword,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!isSuccess && !isTokenExpired) {
            OutlinedTextField(
                value = formFields.newPassword,
                onValueChange = { onFormChanged(formFields.copy(newPassword = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nowe hasło") },
                singleLine = true,
                enabled = !isLoading,
                isError = state is ResetPasswordState.Error,
                visualTransformation = if (formFields.passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onFormChanged(formFields.copy(passwordVisible = !formFields.passwordVisible))
                        }
                    ) {
                        Icon(
                            imageVector = if (formFields.passwordVisible)
                                Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (formFields.passwordVisible)
                                "Ukryj hasło" else "Pokaż hasło"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = formFields.confirmPassword,
                onValueChange = { onFormChanged(formFields.copy(confirmPassword = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Powtórz hasło") },
                singleLine = true,
                enabled = !isLoading,
                isError = state is ResetPasswordState.Error,
                visualTransformation = if (formFields.passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                },
                supportingText = if (state is ResetPasswordState.Error) {
                    { Text(state.message, color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSubmit()
                    }
                ),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            PrimaryButton(
                text = if (isLoading) "Zapisywanie..." else "Ustaw nowe hasło",
                onClick = onSubmit,
                enabled = !isLoading
                    && formFields.newPassword.isNotBlank()
                    && formFields.confirmPassword.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isSuccess) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Przejdź do logowania",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
