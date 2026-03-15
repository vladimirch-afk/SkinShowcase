package ru.kotlix.skinshowcase.message.chats

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    var chatIdToDelete by remember { mutableStateOf<String?>(null) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadChats()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
            onChatLongClick = { chatId -> chatIdToDelete = chatId },
            onRetry = viewModel::loadChats,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }

    if (chatIdToDelete != null) {
        AlertDialog(
            onDismissRequest = { chatIdToDelete = null },
            title = { Text(stringResource(R.string.message_delete_chat)) },
            text = { Text(stringResource(R.string.message_delete_chat_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatIdToDelete?.let { viewModel.deleteChat(it) }
                        chatIdToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.message_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatIdToDelete = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
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
    onChatLongClick: (String) -> Unit = {},
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
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
                        onClick = { onChatClick(chat.id) },
                        onLongClick = if (chat.id != ChatsListViewModel.SUPPORT_CHAT_ID) {
                            { onChatLongClick(chat.id) }
                        } else {
                            { }
                        }
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
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

private val CHAT_CARD_SHAPE = RoundedCornerShape(16.dp)
private val CHAT_AVATAR_SIZE = 52.dp

@Composable
private fun ChatListItem(
    chat: ChatItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayNickname = if (chat.id == ChatsListViewModel.SUPPORT_CHAT_ID) {
        stringResource(R.string.message_support_chat)
    } else {
        chat.nickname
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(chat.id) {
                detectTapGestures(onLongPress = { onLongClick() })
            },
        shape = CHAT_CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(CHAT_AVATAR_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                if (!chat.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = chat.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayNickname,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (chat.id != ChatsListViewModel.SUPPORT_CHAT_ID) {
                    Text(
                        text = chat.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = chat.lastMessage.ifEmpty { " " },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatsListScreenPreview() {
    SkinShowcaseTheme {
        ChatsListScreen(onChatClick = {})
    }
}
