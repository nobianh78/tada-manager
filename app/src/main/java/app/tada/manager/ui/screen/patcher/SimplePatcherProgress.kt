package app.tada.manager.ui.screen.patcher

import android.content.pm.PackageInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tada.manager.R
import app.tada.manager.ui.model.PatchProgressSource
import app.tada.manager.ui.model.State
import app.tada.manager.ui.screen.shared.rememberAccessibilityEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ChatMessage(val id: Int, val text: String, val isUser: Boolean = false)

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
    var isTyping by remember { mutableStateOf(false) }
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

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF1A1410) else Color(0xFFFDF6EC)
    val textColor = if (isDark) Color(0xFFF5EDE3) else Color(0xFF2D1F16)
    val primaryColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFE8930C)
    val cardBg = if (isDark) Color(0xFF2D241D) else Color.White
    val greenStatus = Color(0xFF4CAF50)

    val strStart = stringResource(R.string.chat_patching_start, appName)
    val strUserReply = stringResource(R.string.chat_user_reply)
    val strWait = stringResource(R.string.chat_patching_wait)
    val strComplex = stringResource(R.string.chat_patching_complex)
    val strAlmost = stringResource(R.string.chat_patching_almost)
    val strFinished = stringResource(R.string.chat_patching_finished)

    // Message sequence with safe typing state
    LaunchedEffect(Unit) {
        val messageMutex = Mutex()
        suspend fun sendMessage(text: String, isUser: Boolean = false, typingDuration: Long = 600L) {
            messageMutex.withLock {
                if (!isUser && typingDuration > 0) {
                    isTyping = true
                    try {
                        delay(typingDuration)
                    } finally {
                        isTyping = false
                    }
                }
                messages.add(ChatMessage(nextMessageId++, text, isUser))
            }
        }

        // 1. Initial greeting & start conversation
        sendMessage(strStart, isUser = false, typingDuration = 500L)
        delay(500L)
        sendMessage(strUserReply, isUser = true, typingDuration = 0L)
        delay(400L)
        sendMessage(strWait, isUser = false, typingDuration = 600L)

        // 2. Track milestones & long step warning
        var sentComplex = false
        var sentAlmost = false
        var sent100 = false

        // Observer for long step warning
        launch {
            snapshotFlow { showLongStepWarning }.collect { warning ->
                if (warning && !sentComplex && !sent100) {
                    sentComplex = true
                    sendMessage(strComplex, typingDuration = 700L)
                }
            }
        }

        // Observer for progress milestones
        launch {
            snapshotFlow { progress() }.collect { currentProgress ->
                if (currentProgress >= 0.45f && !sentComplex && !sent100) {
                    sentComplex = true
                    sendMessage(strComplex, typingDuration = 700L)
                }
                if (currentProgress >= 0.80f && !sentAlmost && currentProgress < 1.0f) {
                    sentAlmost = true
                    sendMessage(strAlmost, typingDuration = 600L)
                }
                if (currentProgress >= 1.0f && !sent100) {
                    sent100 = true
                    sendMessage(strFinished, typingDuration = 600L)
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(bgColor).navigationBarsPadding()) {
        queueHeader?.invoke()
        
        // Header Card
        Box(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = cardBg,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.tada_mascot),
                            contentDescription = "Mascot",
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFFFF3E0))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("TADa", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                            val headerStatus = if (currentStep.isNotBlank()) currentStep else stringResource(R.string.chat_header_status, appName)
                            Text(headerStatus, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = greenStatus, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = primaryColor,
                            trackColor = primaryColor.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${(progress() * 100).toInt()}%", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Chat list
        val listState = rememberLazyListState()
        
        LaunchedEffect(messages.size, isTyping) {
            val totalItems = messages.size + if (isTyping) 1 else 0
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    text = msg.text,
                    isUser = msg.isUser,
                    reduceMotion = reduceMotion,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textColor = textColor
                )
            }
            if (isTyping) {
                item(key = "typing") {
                    TypingBubble(reduceMotion = reduceMotion, cardBg = cardBg)
                }
            }
        }
        
        // Fixed bottom bar
        Column(
            modifier = Modifier.fillMaxWidth().background(bgColor).padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = primaryColor,
                    trackColor = primaryColor.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                val bottomStatus = if (currentStep.isNotBlank()) {
                    "${(progress() * 100).toInt()}% • $currentStep"
                } else {
                    stringResource(R.string.chat_bottom_status, (progress() * 100).toInt(), appName)
                }
                Text(
                    text = bottomStatus,
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 220.dp)
                )
            }
        }
    }
}

@Composable
private fun TypingBubble(reduceMotion: Boolean, cardBg: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Image(
            painter = painterResource(R.drawable.tada_mascot),
            contentDescription = null,
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFFF3E0))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .background(
                    color = cardBg,
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (reduceMotion) {
                Text(stringResource(R.string.chat_typing), fontSize = 16.sp, color = Color.Gray)
            } else {
                Text(stringResource(R.string.chat_typing).substringBefore("..."), fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                val infiniteTransition = rememberInfiniteTransition()
                val offsets = List(3) { index ->
                    infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = -6f,
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
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    text: String, 
    isUser: Boolean, 
    reduceMotion: Boolean,
    primaryColor: Color,
    cardBg: Color,
    textColor: Color
) {
    var visible by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { it / 2 })
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
                Image(
                    painter = painterResource(R.drawable.tada_mascot),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFFF3E0))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = if (isUser) primaryColor else cardBg,
                        shape = if (isUser) {
                            RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                        } else {
                            RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = text, 
                    fontSize = 16.sp, 
                    color = if (isUser) Color.White else textColor
                )
            }
        }
    }
}
