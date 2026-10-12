/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 */

package app.tada.manager.ui.screen.home

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.tada.manager.BuildConfig
import app.tada.manager.R
import app.tada.manager.ui.screen.shared.*
import app.tada.manager.ui.viewmodel.UpdateViewModel
import app.tada.manager.util.MANAGER_REPO_URL
import app.tada.manager.util.formatMegabytes
import app.tada.manager.util.isolateLtr
import app.tada.manager.util.releasePageUrl
import app.tada.manager.util.rememberSourceAccent
import app.tada.manager.util.withVersionPrefix
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import kotlin.time.Duration.Companion.milliseconds

/**
 * How long the app must hold the foreground before an install left in
 * [UpdateViewModel.State.INSTALLING] counts as abandoned rather than merely still opening.
 */
private val AbandonedInstallGrace = 1500.milliseconds

/**
 * The distinct bodies the update dialog can show. States that share a body map to the same
 * entry so switching between them does not restart the crossfade.
 */
private enum class UpdateDialogContent {
    DetailsLoading,
    DetailsFailed,
    Details,
    Downloading,
    Installing,
    Failed,
    Success
}

/** Resolves the body to show, the changelog or a stage of the update. */
private fun updateDialogContentOf(updateViewModel: UpdateViewModel): UpdateDialogContent =
    when (updateViewModel.state) {
        UpdateViewModel.State.CAN_DOWNLOAD, UpdateViewModel.State.CAN_INSTALL -> when {
            updateViewModel.changelogError != null -> UpdateDialogContent.DetailsFailed
            updateViewModel.changelogEntries == null -> UpdateDialogContent.DetailsLoading
            else -> UpdateDialogContent.Details
        }

        UpdateViewModel.State.DOWNLOADING -> UpdateDialogContent.Downloading
        UpdateViewModel.State.INSTALLING -> UpdateDialogContent.Installing
        UpdateViewModel.State.FAILED -> UpdateDialogContent.Failed
        UpdateViewModel.State.SUCCESS -> UpdateDialogContent.Success
    }

/**
 * TADa Manager update & changelog dialog.
 * Designed with the adorable TADa mascot, warm cream/amber tones, and user-friendly flows.
 */
@Composable
fun ManagerChangelogDialog(
    onDismiss: () -> Unit,
    updateViewModel: UpdateViewModel = koinViewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    ),
    expectsUpdate: Boolean = false
) {
    val state = updateViewModel.state
    val content = updateDialogContentOf(updateViewModel)
    val hasUpdate = updateViewModel.releaseInfo != null
    val isUpdateUnavailable = expectsUpdate && !hasUpdate && !updateViewModel.isCheckingForUpdate
    val morpheAccent = rememberSourceAccent(isDefault = true, avatarUrl = null, fallbackAvatarUrl = null)
    val older = OlderReleases(
        entries = updateViewModel.olderManagerEntries,
        isLoading = updateViewModel.isLoadingOlderEntries,
        isFailed = updateViewModel.olderEntriesFailed,
        onLoad = updateViewModel::loadOlderManagerEntries
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, updateViewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            delay(AbandonedInstallGrace)
            updateViewModel.resetIfInstallCancelled()
        }
    }

    LaunchedEffect(Unit) {
        updateViewModel.loadChangelog()
    }

    DisposableEffect(Unit) {
        onDispose { updateViewModel.resetOlderManagerEntries() }
    }

    AppDialog(
        onDismissRequest = onDismiss,
        scrollable = false,
        padding = DialogPadding.Compact,
        contentArrangement = Arrangement.Top,
        fillContentHeight = true,
        footer = {
            AnimatedContent(
                targetState = state,
                transitionSpec = Animations.fadeCrossfade(),
                modifier = Modifier.fillMaxWidth(),
                label = "updateFooter"
            ) { footerState ->
                TadaUpdateDialogFooter(
                    state = footerState,
                    updateViewModel = updateViewModel,
                    expectsUpdate = expectsUpdate,
                    onDismiss = onDismiss,
                    translatable = content == UpdateDialogContent.Details
                )
            }
        }
    ) {
        // Cute TADa Mascot Header
        TadaUpdateHeader(
            content = content,
            hasUpdate = hasUpdate,
            currentVersion = BuildConfig.VERSION_NAME,
            targetVersion = updateViewModel.releaseInfo?.version
        )

        AnimatedContent(
            targetState = content,
            transitionSpec = Animations.fadeCrossfade(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = "updateContent"
        ) { currentContent ->
            when (currentContent) {
                UpdateDialogContent.DetailsLoading -> ChangelogListLoading(
                    withSummary = hasUpdate || expectsUpdate,
                    modifier = Modifier.padding(top = Defaults.ItemSpacing)
                )

                UpdateDialogContent.DetailsFailed -> updateViewModel.changelogError?.let { ChangelogError(it) }

                UpdateDialogContent.Details -> {
                    val entries = updateViewModel.changelogEntries.orEmpty()
                    val newReleases = entries.take(updateViewModel.newReleaseCount)
                    ChangelogList(
                        entries = entries,
                        older = older,
                        badges = mapOf(BuildConfig.VERSION_NAME to ChangelogBadge.INSTALLED),
                        contentPadding = PaddingValues(top = Defaults.ItemSpacing),
                        header = when {
                            newReleases.isNotEmpty() -> {
                                {
                                    ChangelogUpdateSummary(
                                        fromVersion = BuildConfig.VERSION_NAME,
                                        toVersion = updateViewModel.releaseInfo?.version
                                            ?: newReleases.first().version,
                                        entries = newReleases
                                    )
                                }
                            }

                            isUpdateUnavailable -> {
                                {
                                    Notice(
                                        icon = Icons.Outlined.HourglassEmpty,
                                        text = stringResource(R.string.manager_update_not_ready),
                                        tone = SemanticTone.Warning
                                    )
                                }
                            }

                            else -> null
                        }
                    )
                }

                UpdateDialogContent.Downloading -> CenteredStatus {
                    TadaDownloadProgress(
                        downloadedSize = updateViewModel.downloadedSize,
                        totalSize = updateViewModel.totalSize,
                        progress = updateViewModel.downloadProgress
                    )
                }

                UpdateDialogContent.Installing -> CenteredStatus {
                    TadaInstallingState()
                }

                UpdateDialogContent.Failed -> CenteredStatus {
                    TadaInstallFailureContent(updateViewModel.installError)
                }

                UpdateDialogContent.Success -> CenteredStatus {
                    TadaUpdateCompletedContent(version = updateViewModel.releaseInfo?.version)
                }
            }
        }
    }

    // Internet check dialog
    if (updateViewModel.showInternetCheckDialog) {
        MeteredDownloadDialog(
            title = stringResource(R.string.download_update_confirmation),
            onConfirm = {
                updateViewModel.showInternetCheckDialog = false
                updateViewModel.downloadUpdate(ignoreInternetCheck = true)
            },
            onDismiss = { updateViewModel.showInternetCheckDialog = false }
        )
    }
}

/**
 * TADa Mascot Header for Update Dialog.
 */
@Composable
private fun TadaUpdateHeader(
    content: UpdateDialogContent,
    hasUpdate: Boolean,
    currentVersion: String,
    targetVersion: String?
) {
    val mascotRes = when (content) {
        UpdateDialogContent.Success -> R.drawable.tada_mascot_celebrate
        UpdateDialogContent.Downloading, UpdateDialogContent.Installing -> R.drawable.tada_mascot_sit
        else -> R.drawable.tada_mascot_wave
    }

    val title = when (content) {
        UpdateDialogContent.Success -> "Cập nhật thành công! 🎉"
        UpdateDialogContent.Downloading -> "Đang tải bản mới nè... 🎋"
        UpdateDialogContent.Installing -> "Đang cài đặt TADa... ⏳"
        UpdateDialogContent.Failed -> "Có lỗi khi cài đặt rồi 🥺"
        else -> if (hasUpdate) "TADa có bản cập nhật mới! ✨" else stringResource(R.string.changelog)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cute Mascot Badge
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    RoundedCornerShape(26.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(mascotRes),
                contentDescription = null,
                modifier = Modifier.size(62.dp)
            )
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LocalDialogTextColor.current,
            textAlign = TextAlign.Center
        )

        // Version comparison pill
        if (hasUpdate && targetVersion != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "v${currentVersion.removePrefix("v")}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = LocalDialogSecondaryTextColor.current
                    )
                    Text(
                        text = "➜",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "v${targetVersion.removePrefix("v")}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            Text(
                text = listOf(
                    stringResource(R.string.app_name),
                    (targetVersion ?: currentVersion).withVersionPrefix().isolateLtr()
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = LocalDialogSecondaryTextColor.current
            )
        }

        // Cute speech bubble banner when update is available in Details view
        if (hasUpdate && content == UpdateDialogContent.Details) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎋", fontSize = 20.sp)
                    Text(
                        text = "Gấu trúc TADa đã chuẩn bị phiên bản mới với nhiều tính năng xịn sò. Cập nhật ngay nhé bạn ơi! ✨",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalDialogTextColor.current,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Modern, friendly download progress UI in TADa style.
 */
@Composable
private fun TadaDownloadProgress(
    downloadedSize: Long,
    totalSize: Long,
    progress: Float
) {
    val percentage = (progress * 100).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Percentage Display
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        // Rounded Linear Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )

        // MB Counter
        val sizeText = if (totalSize > 0L) {
            "${formatMegabytes(downloadedSize)} / ${formatMegabytes(totalSize)}"
        } else if (downloadedSize > 0L) {
            formatMegabytes(downloadedSize)
        } else {
            "Đang kết nối..."
        }

        Text(
            text = sizeText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = LocalDialogSecondaryTextColor.current
        )

        Text(
            text = "Đang tải xuống bản cập nhật APK chính thức từ máy chủ TADa... 🎋",
            style = MaterialTheme.typography.bodySmall,
            color = LocalDialogSecondaryTextColor.current.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Installing state UI in TADa style.
 */
@Composable
private fun TadaInstallingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(54.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 5.dp
        )

        Text(
            text = "Đang mở trình cài đặt hệ thống...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LocalDialogTextColor.current
        )

        Text(
            text = "Hệ thống sẽ hiện thông báo xác nhận, bạn vui lòng chọn 'Cập nhật' hoặc 'Cài đặt' nha! 🎋",
            style = MaterialTheme.typography.bodySmall,
            color = LocalDialogSecondaryTextColor.current,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

/**
 * Install failure UI in TADa style.
 */
@Composable
private fun TadaInstallFailureContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Notice(
            icon = Icons.Outlined.ErrorOutline,
            text = if (message.isNotBlank()) message else "Không thể cài đặt bản cập nhật. Bạn vui lòng thử lại nhé!",
            tone = SemanticTone.Error
        )
    }
}

/**
 * Success confirmation UI in TADa style.
 */
@Composable
private fun TadaUpdateCompletedContent(version: String?) {
    val scale by rememberEntranceScale(
        from = 0.6f,
        dampingRatio = Spring.DampingRatioMediumBouncy,
        label = "tadaSuccessScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = SemanticTone.Success.container,
            modifier = Modifier
                .size(76.dp)
                .scale(scale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                ThemedIcon(
                    icon = Icons.Outlined.CheckCircle,
                    tint = SemanticTone.Success.content,
                    size = 40.dp
                )
            }
        }

        Text(
            text = "TADa đã được cập nhật thành công! 🎋✨",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LocalDialogTextColor.current,
            textAlign = TextAlign.Center
        )

        if (version != null) {
            Text(
                text = "Phiên bản hiện tại: ${version.isolateLtr()}",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalDialogSecondaryTextColor.current,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * TADa Update Dialog Footer with friendly buttons.
 */
@Composable
private fun TadaUpdateDialogFooter(
    state: UpdateViewModel.State,
    updateViewModel: UpdateViewModel,
    expectsUpdate: Boolean,
    onDismiss: () -> Unit,
    translatable: Boolean
) {
    val releaseInfo = updateViewModel.releaseInfo

    val actions: List<DialogAction> = when (state) {
        UpdateViewModel.State.CAN_DOWNLOAD -> buildList {
            if (releaseInfo == null && !expectsUpdate) {
                if (updateViewModel.changelogError != null) {
                    add(
                        DialogAction(
                            text = stringResource(R.string.retry),
                            onClick = { updateViewModel.loadChangelog() },
                            icon = Icons.Outlined.Refresh
                        )
                    )
                }
                add(
                    DialogAction(
                        text = stringResource(R.string.close),
                        onClick = onDismiss,
                        emphasis = DialogActionEmphasis.Outlined
                    )
                )
                return@buildList
            }

            add(
                DialogAction(
                    text = "Tải về & Cài đặt ✨",
                    onClick = { updateViewModel.downloadUpdate() },
                    icon = Icons.Outlined.Download,
                    enabled = releaseInfo != null
                )
            )

            add(
                DialogAction(
                    text = "Để sau nha",
                    onClick = onDismiss,
                    emphasis = DialogActionEmphasis.Outlined
                )
            )

            if (releaseInfo == null && !updateViewModel.isCheckingForUpdate) {
                add(
                    DialogAction(
                        text = stringResource(R.string.retry),
                        onClick = { updateViewModel.retryUpdateCheck() },
                        icon = Icons.Outlined.Refresh
                    )
                )
            }
        }

        UpdateViewModel.State.DOWNLOADING -> listOf(
            DialogAction(
                text = "Ẩn xuống nền",
                onClick = onDismiss,
                emphasis = DialogActionEmphasis.Outlined
            )
        )

        UpdateViewModel.State.CAN_INSTALL -> listOf(
            DialogAction(
                text = "Cài đặt ngay 🚀",
                onClick = { updateViewModel.installUpdate() },
                icon = Icons.Outlined.InstallMobile
            ),
            DialogAction(
                text = "Để sau nha",
                onClick = onDismiss,
                emphasis = DialogActionEmphasis.Outlined
            )
        )

        UpdateViewModel.State.INSTALLING -> emptyList()

        UpdateViewModel.State.FAILED -> listOf(
            DialogAction(
                text = "Thử cài đặt lại",
                onClick = { updateViewModel.installUpdate() },
                icon = Icons.Outlined.InstallMobile
            ),
            DialogAction(
                text = stringResource(android.R.string.cancel),
                onClick = onDismiss,
                emphasis = DialogActionEmphasis.Outlined
            )
        )

        UpdateViewModel.State.SUCCESS -> listOf(
            DialogAction(
                text = "Hoàn tất ✨",
                onClick = onDismiss
            )
        )
    }

    val offersReleasePage = state == UpdateViewModel.State.CAN_DOWNLOAD ||
            state == UpdateViewModel.State.CAN_INSTALL ||
            state == UpdateViewModel.State.FAILED

    val pageUrl = releaseInfo?.pageUrl
        ?: updateViewModel.changelogEntries?.firstOrNull()?.version?.let { releasePageUrl(MANAGER_REPO_URL, it) }

    ChangelogFooter(
        actions = actions,
        translatable = translatable,
        pageUrl = pageUrl.takeIf { offersReleasePage }
    )
}

/** Stands a progress or result in the middle of the room the header leaves. */
@Composable
private fun CenteredStatus(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}

/** Scale that springs up to full size [from] a smaller one once the content enters composition. */
@Composable
private fun rememberEntranceScale(from: Float, dampingRatio: Float, label: String): State<Float> {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    return animateFloatAsState(
        targetValue = if (appeared) 1f else from,
        animationSpec = spring(dampingRatio = dampingRatio, stiffness = Spring.StiffnessLow),
        label = label
    )
}
