/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 */

package app.tada.manager.ui.screen.home

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import app.tada.manager.R
import app.tada.manager.domain.apk.InstalledApkInfo
import app.tada.manager.domain.apk.SavedApkInfo
import app.tada.manager.domain.bundles.*
import app.tada.manager.ui.screen.shared.*
import app.tada.manager.util.androidVersionName
import app.tada.manager.util.htmlAnnotatedString
import app.tada.manager.util.withVersionPrefix
import app.morphe.patcher.patch.AppTarget

/**
 * Dialog 1: Initial "Do you have the APK?" dialog.
 *
 * In expert mode the version list is selectable: the user can tap any version to set it as the
 * download target. [selectedDownloadVersion] reflects the current selection (defaults to
 * [recommendedVersion]); [onVersionSelect] propagates the change to the ViewModel.
 * In simple mode there is only one version and no selection UI is shown.
 */
@Composable
internal fun ApkAvailabilityDialog(
    appName: String,
    packageName: String?,
    recommendedVersion: AppTarget?,
    compatibleVersions: List<BundledAppTarget>,
    selectedDownloadVersion: AppTarget?,
    resolvedDownloadUrl: String?,
    onVersionSelect: (AppTarget) -> Unit,
    usingMountInstall: Boolean,
    stockAppInstalled: Boolean,
    isExpertMode: Boolean,
    savedApkInfo: SavedApkInfo?,
    installedApkInfo: InstalledApkInfo?,
    installedAppVersion: String?,
    onDismiss: () -> Unit,
    onHaveApk: () -> Unit,
    onNeedApk: () -> Unit,
    onUseSaved: () -> Unit,
    onUseInstalled: () -> Unit
) {
    val offeredVersions = remember(compatibleVersions) { compatibleVersions.offered() }

    AppDialog(
        onDismissRequest = onDismiss,
        padding = DialogPadding.None
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(packageName = packageName, contentDescription = null, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp)) // balance the back button
            }

            // Hero section with Panda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.65f).padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Chưa tìm được\nAPK gốc?",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 40.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Để vá $appName, bạn cần APK chưa vá thuộc một trong các phiên bản:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.tada_mascot),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(160.dp)
                        .offset(x = 30.dp, y = (-10).dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }

            // White Card for versions
            SurfaceCard(
                cornerRadius = 24.dp,
                showBorder = false,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .weight(1f, fill = false)
            ) {
                val anyString = stringResource(R.string.any_version)
                val incompatibleSdkVersions = remember(offeredVersions) {
                    offeredVersions.filterNot { it.installableOnDevice() }.mapNotNullTo(mutableSetOf()) { it.target.version }
                }

                if (isExpertMode && offeredVersions.isNotEmpty() && offeredVersions.size > 1) {
                    SelectableVersionListCard(
                        versions = offeredVersions,
                        selectedVersion = selectedDownloadVersion,
                        onVersionSelect = onVersionSelect,
                        anyString = anyString,
                        hasMultipleBundles = offeredVersions.map { it.bundleUid }.distinct().size > 1,
                        incompatibleSdkVersions = incompatibleSdkVersions,
                        savedVersion = savedApkInfo?.version,
                        installedVersion = installedAppVersion,
                    )
                } else {
                    val versionsToList = if (isExpertMode && offeredVersions.isNotEmpty()) {
                        offeredVersions.map { it.target.version ?: anyString }
                    } else {
                        listOf(recommendedVersion?.version ?: anyString)
                    }

                    VersionListCard(
                        versions = versionsToList,
                        experimentalVersions = if (isExpertMode) offeredVersions.experimentalVersions() else emptySet(),
                        descriptions = if (isExpertMode) offeredVersions.mapNotNull { b -> b.target.version?.let { v -> b.target.description?.let { d -> v to d } } }.toMap() else emptyMap(),
                        incompatibleSdkVersions = incompatibleSdkVersions,
                        versionCodes = if (isExpertMode) offeredVersions.mapNotNull { b -> b.target.version?.let { v -> b.buildCodes?.let { v to it } } }.toMap() else compatibleVersions.firstOrNull { it.target.version == recommendedVersion?.version }?.let { b -> b.target.version?.let { v -> b.buildCodes?.let { mapOf(v to it) } } } ?: emptyMap(),
                        savedVersion = savedApkInfo?.version,
                        installedVersion = installedAppVersion,
                        showUnpatchedBadge = !isExpertMode
                    )
                }
            }

            // Buttons at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val preferSavedOverInstalled = savedApkInfo != null && savedApkInfo.version == installedApkInfo?.version

                if (installedApkInfo != null && !preferSavedOverInstalled) {
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.PhoneAndroid, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("Đã cài đặt trên thiết bị này: v${installedApkInfo.version}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                androidx.compose.material3.Button(
                    onClick = onNeedApk,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = resolvedDownloadUrl != null
                ) {
                    if (resolvedDownloadUrl == null) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Outlined.Download, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tải APK giúp tôi", style = MaterialTheme.typography.titleMedium)
                    }
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onHaveApk,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Tôi có APK rồi", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }

                if (savedApkInfo != null) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onUseSaved,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Dùng APK đã lưu v${savedApkInfo.version}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

/**
 * Dialog 3: File picker prompt dialog.
 */
@Composable
internal fun FilePickerPromptDialog(
    appName: String,
    packageName: String?,
    isOtherApps: Boolean,
    isLoadingInstalledApps: Boolean,
    onDismiss: () -> Unit,
    onOpenFilePicker: () -> Unit,
    onUseInstalledApp: (() -> Unit)?
) {
    AppDialog(
        onDismissRequest = onDismiss,
        accentColor = rememberAppColor(packageName),
        title = stringResource(
            if (isOtherApps) {
                R.string.home_select_apk_title
            } else {
                R.string.home_file_picker_prompt_title
            }
        ),
        description = if (isOtherApps) {
            stringResource(R.string.home_select_any_apk_description)
        } else {
            htmlAnnotatedString(stringResource(R.string.home_file_picker_prompt_description, appName))
        },
        footer = {
            AppDialogActions(
                actions = buildList {
                    if (isOtherApps && onUseInstalledApp != null) {
                        add(
                            DialogAction(
                                text = stringResource(R.string.home_use_installed_app),
                                onClick = onUseInstalledApp,
                                icon = Icons.Outlined.PhoneAndroid,
                                enabled = !isLoadingInstalledApps
                            )
                        )
                    }
                    add(
                        DialogAction(
                            text = stringResource(R.string.home_file_picker_prompt_open_apk),
                            onClick = onOpenFilePicker,
                            icon = Icons.Outlined.FolderOpen
                        )
                    )
                    add(
                        DialogAction(
                            text = stringResource(android.R.string.cancel),
                            onClick = onDismiss
                        )
                    )
                },
                layout = DialogButtonLayout.Vertical
            )
        }
    )
}

/**
 * Unsupported version warning dialog.
 */
@Composable
internal fun UnsupportedVersionWarningDialog(
    packageName: String?,
    version: String,
    versionCode: Long? = null,
    recommendedVersion: String?,
    allCompatibleVersions: List<String>,
    versionDescriptions: Map<String, String> = emptyMap(),
    compatibleVersionCodes: Map<String, Set<Int>> = emptyMap(),
    experimentalVersions: Set<String> = emptySet(),
    isExperimental: Boolean = false,
    isExpertMode: Boolean,
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    val versionCodeMismatch = !isExperimental && versionCode != null && version == recommendedVersion
    val tags = versionTagsOf(isExperimental = isExperimental, isUnsupported = !isExperimental)
    // The card is tinted by the same tag it is badged with, so it cannot read as two verdicts
    val tone = tags.firstOrNull()?.tone ?: SemanticTone.Error
    AppDialog(
        onDismissRequest = onDismiss,
        accentColor = rememberAppColor(packageName),
        title = stringResource(R.string.home_dialog_unsupported_version_dialog_title),
        description = stringResource(
            when {
                isExperimental -> R.string.home_dialog_unsupported_version_experimental_description
                versionCodeMismatch -> R.string.home_dialog_unsupported_version_build_mismatch_description
                else -> R.string.home_dialog_unsupported_version_dialog_description
            }
        ),
        padding = DialogPadding.Compact,
        footer = {
            AppDialogButtonRow(
                primaryText = stringResource(R.string.home_dialog_unsupported_version_dialog_proceed),
                onPrimaryClick = onProceed,
                isPrimaryDestructive = true,
                secondaryText = stringResource(android.R.string.cancel),
                onSecondaryClick = onDismiss
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Defaults.ItemSpacing)
        ) {
            VersionPanel(
                color = tone.container.copy(alpha = 0.3f),
                border = CardBorder.tinted(tone.accent),
                header = {
                    CardHeader(
                        title = stringResource(R.string.home_selected_version),
                        accentColor = tone.accent,
                        icon = Icons.Outlined.CheckCircle
                    )
                }
            ) {
                VersionRow(
                    version = version,
                    tags = tags,
                    buildCode = versionCode,
                    emphasized = true,
                    versionColor = tone.accent
                )
            }

            if (isExpertMode && allCompatibleVersions.isNotEmpty()) {
                // Expert mode: every compatible version
                VersionListCard(
                    title = stringResource(R.string.home_dialog_unsupported_version_compatible_versions),
                    icon = Icons.Outlined.Checklist,
                    versions = allCompatibleVersions,
                    recommendedIndex = allCompatibleVersions
                        .indexOfFirst { it !in experimentalVersions }
                        .takeIf { it >= 0 } ?: 0,
                    experimentalVersions = experimentalVersions,
                    descriptions = versionDescriptions,
                    versionCodes = compatibleVersionCodes
                )
            } else if (recommendedVersion != null) {
                // Simple mode or single version: the recommended one alone
                VersionListCard(
                    title = stringResource(R.string.home_recommended_version),
                    icon = VersionTag.Recommended.icon,
                    versions = listOf(recommendedVersion),
                    recommendedIndex = 0,
                    experimentalVersions = experimentalVersions,
                    versionCodes = compatibleVersionCodes
                )
            }
        }
    }
}

/**
 * Warning dialog shown when the selected APK's signing certificate does not match
 * the expected signatures declared in the patch bundle.
 */
@Composable
fun InvalidSignatureDialog(
    appName: String,
    packageName: String?,
    onPickAnother: () -> Unit,
    onProceed: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        accentColor = rememberAppColor(packageName),
        title = stringResource(R.string.home_invalid_signature_title),
        description = htmlAnnotatedString(
            stringResource(R.string.home_invalid_signature_message, appName)
        ),
        footer = {
            AppDialogActions(
                actions = listOf(
                    DialogAction(
                        text = stringResource(R.string.home_split_apk_warning_pick_another),
                        onClick = onPickAnother,
                        icon = Icons.Outlined.FolderOpen
                    ),
                    DialogAction(
                        text = stringResource(R.string.home_dialog_unsupported_version_dialog_proceed),
                        onClick = onProceed
                    ),
                    DialogAction(
                        text = stringResource(android.R.string.cancel),
                        onClick = onDismiss
                    )
                ),
                layout = DialogButtonLayout.Vertical
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Defaults.ContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Notice(
                text = stringResource(R.string.home_invalid_signature_badge),
                tone = SemanticTone.Error,
                icon = Icons.Outlined.Warning
            )
        }
    }
}

/**
 * Warning dialog shown when the user selects a split APK archive (.apks / .apkm / .xapk)
 * for an app that requires a full APK.
 */
@Composable
fun SplitApkWarningDialog(
    appName: String,
    packageName: String?,
    onProceed: () -> Unit,
    onPickAnother: () -> Unit,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        accentColor = rememberAppColor(packageName),
        title = stringResource(R.string.home_split_apk_warning_title),
        description = htmlAnnotatedString(
            stringResource(R.string.home_split_apk_warning_message, appName)
        ),
        footer = {
            AppDialogButtonRow(
                primaryText = stringResource(R.string.home_dialog_unsupported_version_dialog_proceed),
                onPrimaryClick = onProceed,
                secondaryText = stringResource(R.string.home_split_apk_warning_pick_another),
                onSecondaryClick = onPickAnother,
                secondaryIcon = Icons.Outlined.FolderOpen
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Defaults.ContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        }
    }
}

/**
 * Warning dialog shown when the user selects an APK version that is marked experimental
 * in the patch bundle AND experimental-version mode is enabled for that bundle.
 */
@Composable
fun ExperimentalVersionWarningDialog(
    appName: String,
    packageName: String?,
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        accentColor = rememberAppColor(packageName),
        title = stringResource(R.string.morphe_experimental_app_version_dialog_title),
        description = htmlAnnotatedString(
            stringResource(R.string.morphe_experimental_app_version_dialog_message, appName)
        ),
        footer = {
            AppDialogButtonRow(
                primaryText = stringResource(R.string.home_dialog_unsupported_version_dialog_proceed),
                onPrimaryClick = onProceed,
                secondaryText = stringResource(android.R.string.cancel),
                onSecondaryClick = onDismiss
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Defaults.ContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        }
    }
}

/**
 * Wrong package dialog.
 */
@Composable
fun WrongPackageDialog(
    expectedPackage: String,
    actualPackage: String,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        accentColor = rememberAppColor(expectedPackage),
        title = stringResource(R.string.home_dialog_wrong_package_title),
        description = stringResource(R.string.home_dialog_wrong_package_description),
        padding = DialogPadding.Compact,
        footer = {
            AppDialogOutlinedButton(
                text = stringResource(R.string.close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Defaults.ItemSpacing)
            ) {
                // The two are read against each other, so the tone carries which is which
                MonospaceValuePanel(
                    value = expectedPackage,
                    label = stringResource(R.string.home_dialog_expected_package),
                    tone = SemanticTone.Success
                )

                MonospaceValuePanel(
                    value = actualPackage,
                    label = stringResource(R.string.home_dialog_selected_package),
                    tone = SemanticTone.Error
                )
            }
        }
    }
}

/**
 * Shown when the device SDK is lower than the minSdk of every declared AppTarget for this app.
 * Informs the user that their device does not meet the requirements for any supported version.
 */
@Composable
internal fun NoCompatibleVersionsDialog(
    appName: String,
    packageName: String?,
    onDismiss: () -> Unit
) {
    val deviceSdk = Build.VERSION.SDK_INT

    AppDialog(
        onDismissRequest = onDismiss,
        accentColor = rememberAppColor(packageName),
        title = stringResource(R.string.home_apk_no_compatible_versions_title),
        description = htmlAnnotatedString(
            stringResource(
                R.string.home_apk_no_compatible_versions_message,
                appName,
                deviceSdk.androidVersionName(),
                deviceSdk
            )
        ),
        footer = {
            AppDialogOutlinedButton(
                text = stringResource(R.string.close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Defaults.ContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        }
    }
}

/**
 * Version list card where each row is tappable.
 * The selected version gets a checkmark; the recommended version is labeled when not selected.
 * Experimental versions are always labeled regardless of selection state.
 * Versions whose [AppTarget.minSdk] exceeds the current device SDK are shown greyed-out
 * and cannot be selected.
 */
@Composable
private fun SelectableVersionListCard(
    modifier: Modifier = Modifier,
    versions: List<BundledAppTarget>,
    selectedVersion: AppTarget?,
    onVersionSelect: (AppTarget) -> Unit,
    anyString: String,
    hasMultipleBundles: Boolean,
    incompatibleSdkVersions: Set<String> = emptySet(),
    savedVersion: String? = null,
    installedVersion: String? = null
) {
    if (versions.isEmpty()) return

    // The version each source stands behind, experimental ones aside: a source does not
    // recommend a version it marks experimental, whatever the toggle promotes above it
    val recommendedByBundle = remember(versions) {
        versions.groupBy { it.bundleUid }
            .mapValues { (_, section) ->
                section.installable().firstOrNull { !it.target.isExperimental }?.target?.version
            }
    }
    val sourcesByUid = rememberSourcesByUid()
    val selectedLabel = stringResource(R.string.home_selected_version)

    // A card of its own for each source, headed by it and edged in its color, as the patch list
    // blocks out its sources. A lone source needs no heading, the dialog is about it already
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(Defaults.ItemSpacing)
    ) {
        versions.groupBy { it.bundleUid }.forEach { (bundleUid, section) ->
            key(bundleUid) {
                val source = sourcesByUid[bundleUid]
                val sourceColor = source?.let { rememberBundleAccent(it) }
                // Shared by the edge and the header band
                val edgeColor = usableAppAccent(sourceColor).takeIf { hasMultipleBundles }

                VersionPanel(
                    border = if (edgeColor != null) {
                        CardBorder.of(appAccentBorder(sourceColor))
                    } else {
                        CardBorder.neutral
                    },
                    header = if (hasMultipleBundles) {
                        {
                            CardHeader(
                                title = section.first().bundleName,
                                accentColor = edgeColor,
                                leading = source?.let {
                                    { BundleIcon(bundle = it, modifier = Modifier.size(24.dp)) }
                                }
                            )
                        }
                    } else null
                ) {
                    section.forEachIndexed { index, bundled ->
                        val target = bundled.target
                        val versionString = target.version ?: anyString
                        val isIncompatibleSdk = target.version != null && target.version in incompatibleSdkVersions
                        val isSelected = !isIncompatibleSdk && target.version != null &&
                                target.version == selectedVersion?.version
                        val tags = versionTagsOf(
                            requiresAndroidSdk = target.minSdk.takeIf { isIncompatibleSdk },
                            isIncompatible = isIncompatibleSdk && target.minSdk == null,
                            isExperimental = target.isExperimental,
                            isRecommended = !isIncompatibleSdk && target.version != null &&
                                    target.version == recommendedByBundle[bundleUid],
                            isSaved = target.version != null && target.version == savedVersion,
                            isInstalled = target.version != null && target.version == installedVersion
                        )
                        val rowContentDescription = buildString {
                            append(versionString)
                            tags.labels().forEach { append(", $it") }
                            if (isSelected) append(", $selectedLabel")
                            target.description?.let { append(", $it") }
                            if (hasMultipleBundles) append(", ${bundled.bundleName}")
                        }

                        VersionRow(
                            version = versionString,
                            tags = tags,
                            description = target.description,
                            selected = isSelected,
                            enabled = !isIncompatibleSdk,
                            onClick = { onVersionSelect(target) },
                            contentDescription = rowContentDescription
                        )
                        if (index < section.lastIndex) {
                            SettingsDivider()
                        }
                    }
                }
            }
        }
    }
}

/** Versions only read rather than picked from, in one [VersionPanel] with an optional [title]. */
@Composable
private fun VersionListCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    versions: List<String>,
    recommendedIndex: Int = 0,
    showUnpatchedBadge: Boolean = false,
    experimentalVersions: Set<String> = emptySet(),
    descriptions: Map<String, String> = emptyMap(),
    incompatibleSdkVersions: Set<String> = emptySet(),
    versionCodes: Map<String, Set<Int>> = emptyMap(),
    savedVersion: String? = null,
    installedVersion: String? = null
) {
    if (versions.isEmpty()) return

    VersionPanel(
        modifier = modifier,
        // Neutral edge, so a neutral header band
        header = title?.let { { CardHeader(title = it, accentColor = null, icon = icon) } }
    ) {
        versions.forEachIndexed { index, version ->
            val tags = versionTagsOf(
                isIncompatible = version in incompatibleSdkVersions,
                isExperimental = version in experimentalVersions,
                isUnpatched = showUnpatchedBadge && versions.size == 1,
                isRecommended = index == recommendedIndex && !showUnpatchedBadge,
                isSaved = version == savedVersion,
                isInstalled = version == installedVersion
            )
            VersionRow(
                version = version,
                tags = tags,
                buildCode = versionCodes[version]?.firstOrNull()?.toLong(),
                description = descriptions[version],
                enabled = version !in incompatibleSdkVersions,
                emphasized = index == recommendedIndex
            )
            if (index < versions.lastIndex) {
                SettingsDivider()
            }
        }
    }
}

/**
 * A card holding versions, one [VersionRow] after another: a source's own, in the neutral card or
 * edged in its color, or [color] and [border] for a card that stands for a verdict, such as a warning.
 * [header] is usually a [CardHeader].
 */
@Composable
private fun VersionPanel(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
    border: BorderStroke? = CardBorder.neutral,
    header: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Defaults.SettingsCornerRadius),
        color = color,
        tonalElevation = 1.dp,
        border = border
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            header?.invoke()
            content()
        }
    }
}

/**
 * One version of a [VersionPanel]: the version, its build and description, and its tags. A lone
 * badge sits beside the version, where most rows carry theirs, and several go on a line of their
 * own under it, since beside it, they would squeeze the version out of sight. The build and the
 * APKs on hand, see [isOnHand], are no badges but one quiet line under the version.
 *
 * @param selected Whether it is the pick, for a list picked from, which checks it in the app's
 *   color. Null for a list only read, which keeps no room for a check.
 * @param emphasized Sets the version in bold, as the pick or the recommended one is.
 * @param contentDescription What a screen reader announces the row as, in place of its texts.
 */
@Composable
private fun VersionRow(
    version: String,
    tags: List<VersionTag>,
    modifier: Modifier = Modifier,
    buildCode: Long? = null,
    description: String? = null,
    selected: Boolean? = null,
    enabled: Boolean = true,
    emphasized: Boolean = selected == true,
    versionColor: Color = LocalDialogTextColor.current,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val (onHandTags, badgeTags) = tags.partition { it.isOnHand }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (selected != null && onClick != null && enabled) {
                    Modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                } else Modifier
            )
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier)
            .padding(horizontal = Defaults.ContentPadding, vertical = Defaults.ItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(Defaults.ItemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The app's own round check, in its color, as the other pickers mark their pick
        if (selected != null) {
            SelectionCheckIndicator(
                state = if (selected) ToggleableState.On else ToggleableState.Off,
                enabled = enabled
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .then(if (enabled) Modifier else Modifier.alpha(Defaults.DISABLED_ALPHA)),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Defaults.ContentPaddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = version,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal,
                    color = versionColor,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
                if (badgeTags.size == 1) VersionTagBadge(badgeTags.single())
            }
            if (buildCode != null || onHandTags.isNotEmpty()) {
                VersionDetailsLine(buildCode = buildCode, onHandTags = onHandTags)
            }
            if (badgeTags.size > 1) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Defaults.ContentPaddingSmall),
                    verticalArrangement = Arrangement.spacedBy(Defaults.ContentPaddingSmall)
                ) {
                    badgeTags.forEach { VersionTagBadge(it) }
                }
            }
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalDialogSecondaryTextColor.current
                )
            }
        }
    }
}

/**
 * A version's build and the APKs on hand at it, parted by dots in the secondary text color. Only the
 * build number is set in monospace, lining up with the version above it, and its label reads as text.
 */
@Composable
private fun VersionDetailsLine(buildCode: Long?, onHandTags: List<VersionTag>) {
    val color = LocalDialogSecondaryTextColor.current
    val buildText = buildCode?.let { stringResource(R.string.home_dialog_unsupported_version_build, it) }
    val buildLabel = remember(buildText, buildCode) {
        buildText?.let { text ->
            buildAnnotatedString {
                append(text)
                val number = buildCode.toString()
                val start = text.indexOf(number)
                if (start >= 0) {
                    addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, start + number.length)
                }
            }
        }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Defaults.ContentPaddingSmall),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        buildLabel?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = color)
        }
        onHandTags.forEachIndexed { index, tag ->
            if (index > 0 || buildLabel != null) {
                Text(text = "·", style = MaterialTheme.typography.bodySmall, color = color)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = tag.icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(text = tag.label(), style = MaterialTheme.typography.bodySmall, color = color)
            }
        }
    }
}

private fun buildVersionSuffix(version: String, versionCode: Long?): String =
    if (versionCode != null) "v$version ($versionCode)" else "v$version"

