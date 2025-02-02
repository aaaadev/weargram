package moe.astar.telegramw.ui.chat

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.*
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import moe.astar.telegramw.R
import moe.astar.telegramw.Screen
import org.drinkless.tdlib.TdApi
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    navController: NavController,
    chatId: Long,
    threadId: Long?,
    viewModel: ChatViewModel
) {
    val chatState by viewModel.chatState
    LaunchedEffect(chatId) { viewModel.initialize(chatId, threadId) }
    when (chatState) {
        ChatState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        ChatState.Ready -> {
            DisposableEffect(viewModel) {
                if (threadId == null) {
                    viewModel.onStart(chatId)
                }
                onDispose {
                    if (threadId == null) {
                        viewModel.onStop(chatId)
                    }
                }
            }

            ChatScaffold(navController, chatId, threadId, viewModel)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScaffold(
    navController: NavController,
    chatId: Long,
    threadId: Long?,
    viewModel: ChatViewModel
) {
    val messageIds by remember { viewModel.messageProvider.messageIds }.collectAsState()
    val messages by remember { viewModel.messageProvider.messageData }.collectAsState()

    val scrollDirection by viewModel.scrollDirectionFlow.collectAsState()

    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }.distinctUntilChanged().collect {
            viewModel.updateVisibleItems(it)
        }
    }

    Scaffold(
        timeText = {
            TimeText()
        },
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState, modifier = Modifier, reverseDirection = true
            )
        },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            ScalingLazyColumn(state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .onRotaryScrollEvent {
                        coroutineScope.launch {
                            listState.animateScrollBy(it.verticalScrollPixels)
                        }
                        false
                    }
                    .focusRequester(focusRequester)
                    .focusable()
                    .nestedScroll(viewModel.scrollListener)
                    .fillMaxWidth()

            ) {
                item {
                    val scope = rememberCoroutineScope()
                    MessageInput(navController = navController, chatId = chatId, sendMessage = {
                        scope.launch {
                            viewModel.sendMessageAsync(
                                threadId = threadId ?: 0,
                                content = TdApi.InputMessageText(
                                    TdApi.FormattedText(
                                        it, emptyArray()
                                    ), false, false
                                )
                            ).await()
                        }
                    })
                }
                items(
                    messageIds.zip(messageIds.drop(1) + listOf(null)),
                    key = { it }) { (id, prevId) ->
                    messages[id]?.also { message ->
                        val displayDate = messages[prevId]?.let { prevMessage ->
                            val prevDate = Calendar.getInstance()
                                .apply { time = Date(prevMessage.date.toLong() * 1000) }
                            val thisDate = Calendar.getInstance()
                                .apply { time = Date(message.date.toLong() * 1000) }
                            thisDate.get(Calendar.DAY_OF_YEAR) != prevDate.get(Calendar.DAY_OF_YEAR)
                        } ?: true

                        MessageItem(
                            message,
                            messages[prevId],
                            viewModel,
                            navController,
                            displayDate = displayDate,
                            scrollReply = { replyMsgId ->
                                coroutineScope.launch {
                                    listState.animateScrollToItem(
                                        messageIds.indexOf(
                                            replyMsgId
                                        ) + 1
                                    )
                                }
                            }
                        )
                    }
                }

                item {
                    LaunchedEffect(true) {
                        // TODO: make sure this is not looped when end of chat history is reached
                        viewModel.pullMessages()
                    }
                }
            }

            val nearBottom by remember {
                derivedStateOf {
                    listState.layoutInfo.visibleItemsInfo.map { it.index }.contains(0)
                }
            }

            if (scrollDirection < 0 && !nearBottom) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                    .background(Color(0x44000000), CircleShape)) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}

@Composable
fun Sender(
    sender: Long?,
    viewModel: ChatViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    viewModel.getUser(sender)?.also { user ->
        Box(
            modifier = modifier
        ) {
            Text(
                user.let { it.firstName + " " + it.lastName },
                style = MaterialTheme.typography.body2,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.Info.buildRoute("user", user.id))
                }
            )
        }
    }
}

@Composable
fun MessageItem(
    message: TdApi.Message,
    previousMessage: TdApi.Message?,
    viewModel: ChatViewModel,
    navController: NavController,
    displayDate: Boolean = false,
    scrollReply: (Long) -> Unit,
) {

    val chat by viewModel.chatFlow.collectAsState()
    val senderId = message.senderId
    val isGroupChat =
        (chat.type is TdApi.ChatTypeBasicGroup) || (chat.type is TdApi.ChatTypeSupergroup)

    val sender = previousMessage?.senderId?.let {
        if (senderId is TdApi.MessageSenderUser) {
            if (((it is TdApi.MessageSenderUser && it.userId != senderId.userId)
                        || it is TdApi.MessageSenderChat
                        || displayDate)
                && !message.isOutgoing
                && isGroupChat
            ) {
                senderId.userId
            } else null
        } else null
    }
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (displayDate) {
            val locale = LocalContext.current.resources.configuration.locales[0]
            Text(
                text = SimpleDateFormat(
                    "dd MMMM", locale
                ).format(Date(message.date.toLong() * 1000)),
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        MessageContent(
            message,
            viewModel,
            navController,
            scrollReply = scrollReply,
            showSender = sender
        )
    }
}

@Composable
fun MessageInput(
    navController: NavController, chatId: Long, sendMessage: (String) -> Unit = {}
) {

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)
                val activityInput: CharSequence? = results.getCharSequence("input")
                sendMessage(activityInput.toString())
            }
        }

    Row(
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                val remoteInputs: List<RemoteInput> = listOf(
                    RemoteInput.Builder("input").setLabel("Text message?").wearableExtender {
                        setEmojisAllowed(true)
                        setInputActionType(EditorInfo.IME_ACTION_SEND)
                    }.build()
                )
                RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                launcher.launch(intent)
            }, colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primaryVariant,
                contentColor = MaterialTheme.colors.onSurface
            )
        ) {
            Icon(
                painterResource(id = R.drawable.baseline_message_24),
                contentDescription = null,
            )
        }

        Button(
            onClick = { navController.navigate(Screen.ChatMenu.buildRoute(chatId)) },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primaryVariant,
                contentColor = MaterialTheme.colors.onSurface
            )
        ) {
            Icon(
                painterResource(id = R.drawable.baseline_more_horiz_24),
                contentDescription = null,
            )
        }
    }
}