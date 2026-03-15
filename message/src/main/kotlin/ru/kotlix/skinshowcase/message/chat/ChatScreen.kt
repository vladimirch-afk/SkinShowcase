package ru.kotlix.skinshowcase.message.chat

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.kotlix.skinshowcase.designsystem.components.DataErrorDialog
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.message.R
import ru.kotlix.skinshowcase.message.domain.MessageItem
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.chatTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.message_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading && state.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            var messageIdToDelete by remember { mutableStateOf<String?>(null) }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                items(
                    items = state.messages.reversed(),
                    key = { it.id }
                ) { message ->
                    MessageBubble(
                        message = message,
                        onLongClick = { messageIdToDelete = message.id }
                    )
                }
            }
            if (messageIdToDelete != null) {
                AlertDialog(
                    onDismissRequest = { messageIdToDelete = null },
                    title = { Text(stringResource(R.string.message_delete_message)) },
                    text = { Text(stringResource(R.string.message_delete_message_confirm)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                messageIdToDelete?.let { viewModel.deleteMessage(it) }
                                messageIdToDelete = null
                            }
                        ) {
                            Text(stringResource(R.string.message_delete), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { messageIdToDelete = null }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    }
                )
            }
            ChatInputRow(
                draft = state.messageDraft,
                onDraftChange = viewModel::updateDraft,
                onSend = viewModel::sendMessage,
                sending = state.isSending
            )
        }
    }

    if (state.errorMessage != null) {
        val context = LocalContext.current
        DataErrorDialog(
            title = stringResource(R.string.error_data_title),
            message = stringResource(R.string.error_data_message),
            okText = stringResource(R.string.error_dialog_ok),
            settingsText = stringResource(R.string.error_dialog_settings),
            onDismiss = viewModel::clearError,
            onOpenSettings = {
                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
        )
    }
}

@Composable
private fun ChatInputRow(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    sending: Boolean,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.message_input_hint)) },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend(); keyboardController?.hide() }),
            singleLine = false
        )
        Button(
            onClick = {
                onSend()
                keyboardController?.hide()
            },
            enabled = draft.isNotBlank() && !sending
        ) {
            if (sending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.message_send))
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageItem,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val contentAlignment = if (message.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.isOutgoing) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            ),
        horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.CenterVertically),
            contentAlignment = contentAlignment
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    SkinShowcaseTheme {
        ChatScreen(chatId = "1", onBack = { })
    }
}
