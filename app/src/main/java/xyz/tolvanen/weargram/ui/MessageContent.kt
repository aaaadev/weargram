package xyz.tolvanen.weargram.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.Location

@Composable
fun MessageContent(message: TdApi.Message, viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val content = message.content
    Log.d("MessageContent", "top")
    Log.d("MessageContent", "kek")
    when (content) {
        is TdApi.MessageText -> TextMessage(content, modifier)
        is TdApi.MessagePhoto -> PhotoMessage(content, viewModel, modifier)
        is TdApi.MessageAudio -> AudioMessage(content, modifier)
        is TdApi.MessageVideo -> VideoMessage(content, modifier)
        is TdApi.MessageSticker -> StickerMessage(content, modifier)
        is TdApi.MessageDocument -> DocumentMessage(content, modifier)
        is TdApi.MessageLocation -> LocationMessage(content, modifier)
        is TdApi.MessageAnimatedEmoji -> AnimatedEmojiMessage(content, modifier)
        is TdApi.MessageAnimation -> AnimationMessage(content, modifier)
        is TdApi.MessageCall -> CallMessage(content, modifier)
        is TdApi.MessagePoll -> PollMessage(content, modifier)
        else -> UnsupportedMessage(content, modifier)
    }

}


@Composable
fun TextMessage(content: TdApi.MessageText, modifier: Modifier = Modifier) {
    Text(
        text = content.text.text,
        modifier = modifier,
        style = MaterialTheme.typography.body2
    )
}
@Composable
fun PhotoMessage(content: TdApi.MessagePhoto, viewModel: ChatViewModel, modifier: Modifier = Modifier) {
    val image= remember { viewModel.fetchPhoto(content) }.collectAsState(initial = null)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        image.value?.also {
            Image(
                //modifier = Modifier.fillMaxSize(),
                bitmap = it,
                contentDescription = null
            )
        }
        content.caption.text.takeIf { it.isNotEmpty() }?.let {
            Text(
                text = it,
                modifier = modifier,
                style = MaterialTheme.typography.body2
            )

        }

    }

}

@Composable
fun AudioMessage(content: TdApi.MessageAudio, modifier: Modifier = Modifier) {
    Text("Audio", modifier = modifier)
}

@Composable
fun VideoMessage(content: TdApi.MessageVideo, modifier: Modifier = Modifier) {
    Text("Video", modifier = modifier)
}

@Composable
fun StickerMessage(content: TdApi.MessageSticker, modifier: Modifier = Modifier) {
    Text(content.sticker.emoji + " Sticker", modifier = modifier)
}

@Composable
fun DocumentMessage(content: TdApi.MessageDocument, modifier: Modifier = Modifier) {
    Text("file: " + content.document.fileName, modifier = modifier)
}

@Composable
fun LocationMessage(content: TdApi.MessageLocation, modifier: Modifier = Modifier) {
    Text("Location: lat ${content.location.latitude}, lon ${content.location.longitude}", modifier = modifier)
}

@Composable
fun AnimatedEmojiMessage(content: TdApi.MessageAnimatedEmoji, modifier: Modifier = Modifier) {
    Text(content.emoji, modifier = modifier)
}

@Composable
fun AnimationMessage(content: TdApi.MessageAnimation, modifier: Modifier = Modifier) {
    Text("Animation", modifier = modifier)
}

@Composable
fun CallMessage(content: TdApi.MessageCall, modifier: Modifier = Modifier) {
    Text("Call", modifier = modifier)
}

@Composable
fun PollMessage(content: TdApi.MessagePoll, modifier: Modifier = Modifier) {
    Text("Poll", modifier = modifier)
}
@Composable
fun UnsupportedMessage(content: TdApi.MessageContent, modifier: Modifier = Modifier) {
    Text("Unsupported message", modifier = modifier)

}
