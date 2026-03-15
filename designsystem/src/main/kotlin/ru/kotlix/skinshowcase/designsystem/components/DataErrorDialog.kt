package ru.kotlix.skinshowcase.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Диалог ошибки получения данных с кнопками «ОК» и «Настройки».
 * Тексты задаются через [title], [message], [okText], [settingsText].
 */
@Composable
fun DataErrorDialog(
    title: String,
    message: String,
    okText: String,
    settingsText: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(okText)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onOpenSettings()
                onDismiss()
            }) {
                Text(settingsText)
            }
        }
    )
}
