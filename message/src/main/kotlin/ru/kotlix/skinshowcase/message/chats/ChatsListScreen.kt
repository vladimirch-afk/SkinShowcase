package ru.kotlix.skinshowcase.message.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.message.R
import ru.kotlix.skinshowcase.message.domain.ChatItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    onChatClick: (String) -> Unit = {},
    viewModel: ChatsListViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var showNewChatDialog by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.message_chats_title)) },
                actions = {
                    IconButton(onClick = { viewModel.loadChats() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.message_refresh)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.message_new_chat)
                )
            }
        }
    ) { innerPadding ->
        ChatsListContent(
            state = state,
            onChatClick = onChatClick,
            onRetry = viewModel::loadChats,
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { showNewChatDialog = false },
            onOpenChat = { steamId ->
                showNewChatDialog = false
                if (steamId.isNotBlank()) onChatClick(steamId.trim())
            }
        )
    }
}

@Composable
private fun ChatsListContent(
    state: ChatsListUiState,
    onChatClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading && state.chats.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null && state.chats.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.message_retry))
                }
            }
        }
        return
    }

    if (state.chats.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.message_empty_chats),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = state.chats,
            key = { it.id }
        ) { chat ->
            ChatListItem(
                chat = chat,
                onClick = { onChatClick(chat.id) }
            )
        }
    }
}

private const val STEAM_ID_LENGTH = 17

private fun isValidSteamId(value: String): Boolean =
    value.length == STEAM_ID_LENGTH && value.all { it.isDigit() }

@Composable
private fun NewChatDialog(
    onDismiss: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    var steamId by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val trimmed = steamId.trim()
    val valid = isValidSteamId(trimmed)
    val showError = trimmed.isNotEmpty() && !valid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.message_new_chat)) },
        text = {
            OutlinedTextField(
                value = steamId,
                onValueChange = { new -> steamId = new.filter { it.isDigit() }.take(STEAM_ID_LENGTH) },
                label = { Text(stringResource(R.string.message_steam_id_hint)) },
                supportingText = if (showError) {
                    { Text(stringResource(R.string.message_steam_id_invalid), color = MaterialTheme.colorScheme.error) }
                } else null,
                isError = showError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (valid) onOpenChat(trimmed)
                },
                enabled = valid
            ) {
                Text(stringResource(R.string.message_open_chat))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun ChatListItem(
    chat: ChatItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = chat.title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatsListScreenPreview() {
    SkinShowcaseTheme {
        ChatsListScreen(onChatClick = {})
    }
}
