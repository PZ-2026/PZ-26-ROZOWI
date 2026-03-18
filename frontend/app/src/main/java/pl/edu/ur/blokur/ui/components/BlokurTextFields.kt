package pl.edu.ur.blokur.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme
import pl.edu.ur.blokur.ui.theme.NeutralSurface2
import pl.edu.ur.blokur.ui.theme.Stroke

@Composable
fun BlokurTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier.fillMaxWidth(),
        label = {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        singleLine            = singleLine,
        enabled               = enabled,
        keyboardOptions       = keyboardOptions,
        visualTransformation  = visualTransformation,
        leadingIcon           = leadingIcon,
        trailingIcon          = trailingIcon,
        shape        = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor        = NeutralSurface2,
            unfocusedContainerColor      = NeutralSurface2,
            disabledContainerColor       = Color(0xFFF3F4F6),
            focusedIndicatorColor        = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor      = Stroke,
            disabledIndicatorColor       = Stroke,
            focusedLabelColor            = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor          = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor           = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            focusedTextColor             = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor           = MaterialTheme.colorScheme.onSurface,
            disabledTextColor            = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            cursorColor                  = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor      = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor    = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor     = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor   = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun BlokurTextFieldPreview() {
    BlokurPreviewTheme {
        BlokurTextField(
            value         = "Jan Kowalski",
            onValueChange = {},
            label         = "Imię i nazwisko",
            modifier      = Modifier.padding(16.dp)
        )
    }
}