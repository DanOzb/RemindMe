package com.example.remindme.ui.reminder

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = RemindTheme.colors

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = RemindShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.textBrand,
            contentColor = colors.textOnBrand,
            disabledContainerColor = colors.textBrand.copy(alpha = 0.4f),
            disabledContentColor = colors.textOnBrand.copy(alpha = 0.7f)
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@PreviewLightDark
@Composable
private fun PrimaryButtonPreview() {
    PreviewSurface {
        PrimaryButton(text = "Create reminder", onClick = {}, icon = Icons.Default.Check)
        PrimaryButton(text = "Create reminder", onClick = {}, icon = Icons.Default.Check, enabled = false)
    }
}
