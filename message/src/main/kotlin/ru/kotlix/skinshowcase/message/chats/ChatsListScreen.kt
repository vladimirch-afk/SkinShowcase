package ru.kotlix.skinshowcase.message.chats

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.kotlix.skinshowcase.designsystem.components.DataErrorDialog
import ru.kotlix.skinshowcase.designsystem.theme.SkinShowcaseTheme
import ru.kotlix.skinshowcase.message.R
import ru.kotlix.skinshowcase.message.domain.ChatItem

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
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
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
    // Всегда LazyColumn, чтобы PullToRefreshBox распознавал жест (нужен скролл).
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when {
            state.isLoading && state.chats.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            state.errorMessage != null && state.chats.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.error_data_title),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            state.chats.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.message_empty_chats),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            else -> {
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
        // Чтобы список всегда скроллился и PullToRefresh срабатывал.
        item {
            Spacer(modifier = Modifier.height(400.dp))
        }
    }
}

private const val STEAM_ID_LENGTH = 17

private fun isValidSteamId(value: String): Boolean =
    value.length == STEAM_ID_LENGTH && value.all { it.isDigit() }

/** Принимает и Steam ID (17 цифр), и ник/имя (любой непустой текст). */
private fun isSteamIdOrNickValid(value: String): Boolean = value.isNotBlank()

@Composable
private fun NewChatDialog(
    onDismiss: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val trimmed = query.trim()
    val valid = isSteamIdOrNickValid(trimmed)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.message_new_chat)) },
        text = {
            OutlinedTextField(
                value = query,
                onValueChange = { new -> query = new },
                label = { Text(stringResource(R.string.message_steam_id_hint)) },
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
    val displayTitle = if (chat.id == ChatsListViewModel.SUPPORT_CHAT_ID) {
        stringResource(R.string.message_support_chat)
    } else {
        chat.title
    }
    ListItem(
        headlineContent = {
            Text(
                text = displayTitle,
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
