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
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.PrimaryButton
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.views.auth.utils.ForgotPasswordFormFields
import pl.edu.ur.blokur.ui.views.auth.utils.ForgotPasswordState

@Composable
fun ForgotPasswordForm(
    state: ForgotPasswordState,
    formFields: ForgotPasswordFormFields,
    onFormChanged: (ForgotPasswordFormFields) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isLoading = state is ForgotPasswordState.Loading
    val isSuccess = state is ForgotPasswordState.Success

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
            text = "Zapomniałem hasła",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Podaj adres e-mail powiązany z Twoim kontem. Wyślemy Ci 6-cyfrowy kod do resetowania hasła.",
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
                    text = (state as? ForgotPasswordState.Success)?.message ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuccessGreen,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (!isSuccess) {
            OutlinedTextField(
                value = formFields.email,
                onValueChange = { onFormChanged(formFields.copy(email = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Adres e-mail") },
                singleLine = true,
                enabled = !isLoading,
                isError = state is ForgotPasswordState.Error,
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Email, contentDescription = null)
                },
                supportingText = if (state is ForgotPasswordState.Error) {
                    { Text(state.message, color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
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
                text = if (isLoading) "Wysyłanie..." else "Wyślij kod resetujący",
                onClick = onSubmit,
                enabled = !isLoading && formFields.email.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = if (isSuccess) "Wróć do logowania" else "Wróć",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
