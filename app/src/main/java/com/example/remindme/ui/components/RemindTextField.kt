package com.example.remindme.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit

@Composable
fun RemindTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val colors = RemindTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = colors.textSecondary) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = trailingContent,
        singleLine = true,
        shape = RemindShapes.field,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = imeAction
        ),
        keyboardActions = onImeAction?.let { action -> KeyboardActions(onAny = { action() }) } ?: KeyboardActions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.bgReminderCard,
            unfocusedContainerColor = colors.bgReminderCard,
            focusedBorderColor = colors.textBrand,
            unfocusedBorderColor = colors.border,
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text,
            cursorColor = colors.textBrand
        )
    )
}

@PreviewLightDark
@Composable
private fun RemindTextFieldPreview() {
    PreviewSurface {
        RemindTextField(value = "", onValueChange = {}, placeholder = "Buy apples", leadingIcon = Icons.Outlined.Edit)
        RemindTextField(value = "Call Mum", onValueChange = {}, placeholder = "Buy apples", leadingIcon = Icons.Outlined.Edit)
    }
}
