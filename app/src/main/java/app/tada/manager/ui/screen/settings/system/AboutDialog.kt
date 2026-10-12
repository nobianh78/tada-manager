/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 */

package app.tada.manager.ui.screen.settings.system

import android.view.HapticFeedbackConstants
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tada.manager.BuildConfig
import app.tada.manager.R
import app.tada.manager.ui.screen.shared.*
import app.tada.manager.ui.viewmodel.ThemeSettingsViewModel
import app.tada.manager.util.isolateLtr
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Github
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

// Taps on the version needed to be offered the pills, the count Android itself asks for
private const val PILL_TAP_COUNT = 7

// Counting stops after a pause, so the seventh tap has to belong to one deliberate run
private val PILL_TAP_TIMEOUT = 1500.milliseconds

/**
 * About dialog.
 * Shows app icon, version, description, social links, and credits button.
 */
@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    themeViewModel: ThemeSettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val view = LocalView.current

    val showCreditsDialog = remember { mutableStateOf(false) }
    val showPillDialog = remember { mutableStateOf(false) }
    val matrixUnlocked by themeViewModel.prefs.matrixBackgroundUnlocked.getAsState()
    var versionTaps by remember { mutableIntStateOf(0) }

    LaunchedEffect(versionTaps) {
        if (versionTaps == 0) return@LaunchedEffect

        delay(PILL_TAP_TIMEOUT)
        versionTaps = 0
    }

    if (showCreditsDialog.value) {
        CreditsDialog(onDismiss = { showCreditsDialog.value = false })
    }

    if (showPillDialog.value) {
        RedPillDialog(
            alreadyUnlocked = matrixUnlocked,
            onRedPill = { themeViewModel.unlockMatrixBackground() },
            // Waking up puts the rain back out of sight, and the pills stay where they were found
            onBluePill = { if (matrixUnlocked) themeViewModel.forgetMatrixBackground() },
            onDismiss = { showPillDialog.value = false }
        )
    }

    AppDialog(
        onDismissRequest = onDismiss,
        footer = {
            AppDialogActions(
                actions = listOf(
                    DialogAction(
                        text = stringResource(R.string.credits),
                        onClick = { showCreditsDialog.value = true },
                        icon = Icons.Outlined.People,
                        emphasis = DialogActionEmphasis.Outlined
                    ),
                    DialogAction(
                        text = stringResource(R.string.close),
                        onClick = onDismiss
                    )
                ),
                layout = DialogButtonLayout.Vertical
            )
        }
    ) {
        val textColor = LocalDialogTextColor.current
        val secondaryColor = LocalDialogSecondaryTextColor.current

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Mascot header in a soft cute rounded container
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.tada_mascot_wave),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(92.dp)
                )
            }

            // App Name & Version
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.version) + " " + BuildConfig.VERSION_NAME.isolateLtr(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            versionTaps++
                            when {
                                versionTaps >= PILL_TAP_COUNT -> {
                                    versionTaps = 0
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    showPillDialog.value = true
                                }
                                // The halfway point is where the taps start answering back,
                                // enough of a hint to keep going without naming what is coming
                                versionTaps > PILL_TAP_COUNT / 2 ->
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Description
            Text(
                text = stringResource(R.string.settings_system_manager_description),
                style = MaterialTheme.typography.bodyLarge,
                color = secondaryColor,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            // Current source code
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_current_source),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryColor
                )
                SettingsGroup {
                    SettingsItem(
                        icon = FontAwesomeIcons.Brands.Github,
                        title = "TADa Manager",
                        subtitle = "github.com/nobianh78/tada-manager",
                        onClick = { uriHandler.openUri("https://github.com/nobianh78/tada-manager") }
                    )
                }
            }

            // Developed based on source code
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_based_on_source),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryColor
                )
                SettingsGroup {
                    SettingsItem(
                        icon = FontAwesomeIcons.Brands.Github,
                        title = "Morphe",
                        subtitle = "github.com/morpheapp",
                        onClick = { uriHandler.openUri("https://github.com/morpheapp") }
                    )
                    SettingsDivider()
                    SettingsItem(
                        icon = FontAwesomeIcons.Brands.Github,
                        title = "ReVanced",
                        subtitle = "github.com/ReVanced/revanced-manager",
                        onClick = { uriHandler.openUri("https://github.com/ReVanced/revanced-manager") }
                    )
                }
            }

        }
    }
}
