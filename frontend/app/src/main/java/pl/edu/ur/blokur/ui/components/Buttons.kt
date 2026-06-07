package pl.edu.ur.blokur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.PreviewTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun FloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    if (label != null) {
        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp
            ),
            icon = { Icon(imageVector = icon, contentDescription = contentDescription) },
            text = { Text(text = label, style = MaterialTheme.typography.labelLarge) }
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp
            )
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlokurButtonsPreview() {
    PreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PrimaryButton(
                text = "Zapisz",
                onClick = {},
            )
            SecondaryButton(
                text = "Anuluj",
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlokurFabPreview() {
    PreviewTheme {
        FloatingActionButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Dodaj",
            onClick = {}
        )
    }
}

