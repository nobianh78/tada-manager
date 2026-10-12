/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 */

package app.tada.manager.ui.screen.settings.system

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tada.manager.BuildConfig
import app.tada.manager.R
import app.tada.manager.ui.screen.shared.*
import app.tada.manager.util.isolateLtr
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Github

/**
 * Credits dialog.
 * Shows current and prior development repositories and open-source licenses.
 */
@Composable
fun CreditsDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val showLicensesDialog = remember { mutableStateOf(false) }

    if (showLicensesDialog.value) {
        LicensesDialog(onDismiss = { showLicensesDialog.value = false })
    }

    AppDialog(
        onDismissRequest = onDismiss,
        footer = {
            AppDialogOutlinedButton(
                text = stringResource(R.string.close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        val textColor = LocalDialogTextColor.current
        val secondaryColor = LocalDialogSecondaryTextColor.current

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Mascot header in a soft cute rounded container
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.tada_mascot_celebrate),
                    contentDescription = stringResource(R.string.credits),
                    modifier = Modifier.size(84.dp)
                )
            }

            // Title & App Version
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.credits),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = stringResource(R.string.app_name) + " " + BuildConfig.VERSION_NAME.isolateLtr(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryColor
                )
            }

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

            // Licenses
            SettingsGroup {
                SettingsItem(
                    onClick = { showLicensesDialog.value = true },
                    icon = Icons.AutoMirrored.Outlined.Article,
                    title = stringResource(R.string.opensource_licenses)
                )
            }
        }
    }
}
