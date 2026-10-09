package app.tada.manager.ui.screen.patcher

import android.content.pm.PackageInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tada.manager.R
import app.tada.manager.ui.model.PatchProgressSource
import app.tada.manager.ui.model.State
import app.tada.manager.ui.screen.shared.rememberAccessibilityEnabled
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(val id: Int, val text: String, val timestamp: Long = System.currentTimeMillis())

@Composable
fun SimplePatchingInProgress(
    progress: () -> Float,
    patchesProgress: Pair<Int, Int>,
    patchProgress: PatchProgressSource,
    packageName: String?,
    showLongStepWarning: Boolean,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    queueHeader: (@Composable () -> Unit)? = null
) {
    val completedPatches = patchesProgress.first
    val totalPatches = patchesProgress.second
    
    val currentStep by remember(patchProgress) { derivedStateOf { patchProgress.steps.firstOrNull { it.state == State.RUNNING }?.name ?: "" } }
    
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var isTyping by remember { mutableStateOf(true) }
    var nextMessageId by remember { mutableStateOf(0) }
    
    val reduceMotion = rememberAccessibilityEnabled()
    
    val context = LocalContext.current
    val pm = context.packageManager
    
    val appName = try {
        if (packageName != null) {
            pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString()
        } else {
            "App"
        }
    } catch (e: Exception) {
        "App"
    }

    // Initial message
    LaunchedEffect(Unit) {
        delay(1000)
        messages.add(ChatMessage(nextMessageId++, context.getString(R.string.chat_patching_start, appName)))
        isTyping = false
    }

    var trigger25 by remember { mutableStateOf(false) }
    var trigger55 by remember { mutableStateOf(false) }
    var trigger85 by remember { mutableStateOf(false) }

    LaunchedEffect(progress()) {
        val currentProgress = progress()
        if (currentProgress >= 0.25f && !trigger25) {
            trigger25 = true
            isTyping = true
            delay(1500)
            messages.add(ChatMessage(nextMessageId++, context.getString(R.string.chat_patching_25)))
            isTyping = false
        }
        if (currentProgress >= 0.55f && !trigger55) {
            trigger55 = true
            isTyping = true
            delay(1500)
            messages.add(ChatMessage(nextMessageId++, context.getString(R.string.chat_patching_55, completedPatches, totalPatches)))
            isTyping = false
        }
        if (currentProgress >= 0.85f && !trigger85) {
            trigger85 = true
            isTyping = true
            delay(1500)
            messages.add(ChatMessage(nextMessageId++, context.getString(R.string.chat_patching_85)))
            isTyping = false
        }
    }

    Column(modifier = modifier.fillMaxSize().navigationBarsPadding()) {
        queueHeader?.invoke()
        
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.tada_mascot),
                contentDescription = "Mascot",
                modifier = Modifier.size(52.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("TADa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(currentStep, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        // LinearProgressIndicator
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("${(progress() * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chat list
        val listState = rememberLazyListState()
        
        // Auto-scroll to bottom
        LaunchedEffect(messages.size, isTyping) {
            val totalItems = messages.size + if (isTyping) 1 else 0
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    text = msg.text,
                    timestamp = msg.timestamp,
                    reduceMotion = reduceMotion
                )
            }
            if (isTyping) {
                item(key = "typing") {
                    TypingBubble(reduceMotion = reduceMotion)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
        
        // Cancel button
        PatcherBottomActionBar(
            horizontalPadding = 16.dp,
            showHomeButton = false,
            onCancelClick = onCancelClick
        )
    }
}

@Composable
private fun TypingBubble(reduceMotion: Boolean) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (reduceMotion) {
            Text(stringResource(R.string.chat_typing), style = MaterialTheme.typography.bodyLarge)
        } else {
            val infiniteTransition = rememberInfiniteTransition()
            val offsets = List(3) { index ->
                infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(300, delayMillis = index * 100, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "typing_bounce"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                offsets.forEach { offset ->
                    Box(
                        modifier = Modifier
                            .offset(y = offset.value.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(text: String, timestamp: Long, reduceMotion: Boolean) {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = remember(timestamp) { formatter.format(Date(timestamp)) }

    var visible by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { it / 2 })
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeString,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
