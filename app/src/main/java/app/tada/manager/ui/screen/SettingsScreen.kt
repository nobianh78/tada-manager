/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 */

package app.tada.manager.ui.screen

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import app.tada.manager.R
import app.tada.manager.domain.manager.SettingsSection
import app.tada.manager.ui.screen.home.GlobalOnboardingState
import app.tada.manager.ui.screen.home.ManagerChangelogDialog
import app.tada.manager.ui.screen.settings.AdvancedTabContent
import app.tada.manager.ui.screen.settings.AppearanceTabContent
import app.tada.manager.ui.screen.settings.SystemTabContent
import app.tada.manager.ui.screen.settings.system.*
import app.tada.manager.ui.screen.shared.*
import app.tada.manager.ui.viewmodel.*
import app.tada.manager.util.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Unified settings screen with a continuous Card UI.
 */
@Composable
fun SettingsScreen(
    homeViewModel: HomeViewModel,
    themeViewModel: ThemeSettingsViewModel = koinViewModel(),
    importExportViewModel: ImportExportViewModel = koinViewModel(),
    patchOptionsViewModel: PatchOptionsViewModel = koinViewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    ),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    globalOnboardingState: GlobalOnboardingState? = null,
    onStartTour: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isTV = remember { context.isAndroidTv() }
    val wrongCredentialsText = stringResource(R.string.settings_system_import_keystore_wrong_credentials)

    val mainScrollState = rememberScrollState()

    var themeSelectorScrollTarget by remember { mutableIntStateOf(0) }
    var expertModeScrollTarget by remember { mutableIntStateOf(0) }
    var installerScrollTarget by remember { mutableIntStateOf(0) }
    var processRuntimeScrollTarget by remember { mutableIntStateOf(0) }
    var filePickerScrollTarget by remember { mutableIntStateOf(0) }

    // Register scroll/navigate callbacks for onboarding
    LaunchedEffect(globalOnboardingState) {
        globalOnboardingState?.let { obs ->
            obs.onNavigateToAppearanceTab = {
                coroutineScope.launch { mainScrollState.animateScrollTo(0) }
            }
            obs.onNavigateToSystemTab = {
                // Approximate scrolling to system tab section
                coroutineScope.launch { mainScrollState.animateScrollTo(installerScrollTarget) }
            }
            obs.onScrollToThemeSelector = {
                coroutineScope.launch { mainScrollState.animateScrollTo(themeSelectorScrollTarget) }
            }
            obs.onScrollToExpertMode = {
                coroutineScope.launch { mainScrollState.animateScrollTo(expertModeScrollTarget) }
            }
            obs.onScrollToInstaller = {
                coroutineScope.launch { mainScrollState.animateScrollTo(installerScrollTarget) }
            }
            obs.onScrollToProcessRuntime = {
                coroutineScope.launch { mainScrollState.animateScrollTo(processRuntimeScrollTarget) }
            }
            obs.onScrollToFilePicker = {
                coroutineScope.launch { mainScrollState.animateScrollTo(filePickerScrollTarget) }
            }
        }
    }

    DisposableEffect(globalOnboardingState) {
        onDispose {
            globalOnboardingState?.let { obs ->
                obs.onNavigateToAppearanceTab = null
                obs.onNavigateToSystemTab = null
                obs.onScrollToThemeSelector = null
                obs.onScrollToExpertMode = null
                obs.onScrollToInstaller = null
                obs.onScrollToProcessRuntime = null
                obs.onScrollToFilePicker = null
            }
        }
    }

    val theme by themeViewModel.prefs.theme.getAsState()
    val themeStyle by themeViewModel.prefs.themeStyle.getAsState()
    val pureBlackTheme by themeViewModel.prefs.pureBlackTheme.getAsState()
    val customAccentColorHex by themeViewModel.prefs.customAccentColor.getAsState()

    val showAboutDialog = rememberSaveable { mutableStateOf(false) }
    val showInstallerDialog = remember { mutableStateOf(false) }
    val showChangelogDialog = remember { mutableStateOf(false) }

    val importKeystoreLauncher = rememberAdaptiveFilePicker(
        mimeTypes = arrayOf("*/*"),
        customPickerMimeTypes = arrayOf(
            "application/x-pkcs12",
            "application/x-java-keystore",
            "application/vnd.morphe.keystore",
        ),
        onResult = { uri -> uri?.let { importExportViewModel.startKeystoreImport(it) } }
    )

    var pendingSettingsImportUri by remember { mutableStateOf<Uri?>(null) }
    val importSettingsLauncher = rememberAdaptiveFilePicker(
        mimeTypes = arrayOf(JSON_MIMETYPE, TEXT_MIMETYPE),
        customPickerMimeTypes = arrayOf(JSON_MIMETYPE),
        onResult = { uri ->
            uri ?: return@rememberAdaptiveFilePicker
            val sections = importExportViewModel.settingsSections
            if (SettingsSection.SOURCES in sections || SettingsSection.PATCH_SELECTIONS in sections) {
                pendingSettingsImportUri = uri
            } else {
                importExportViewModel.importManagerSettings(uri)
            }
        }
    )

    val exportKeystoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri -> uri?.let { importExportViewModel.exportKeystore(it) } }

    val exportSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(JSON_MIMETYPE)
    ) { uri -> uri?.let { importExportViewModel.exportManagerSettings(it) } }

    val exportDebugLogsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(TEXT_MIMETYPE)
    ) { uri -> uri?.let { importExportViewModel.exportDebugLogs(it) } }

    if (showAboutDialog.value) {
        AboutDialog(onDismiss = { showAboutDialog.value = false })
    }

    if (importExportViewModel.showCredentialsDialog) {
        KeystoreCredentialsDialog(
            onDismiss = {
                importExportViewModel.cancelKeystoreImport()
            },
            initialFormat = importExportViewModel.detectedKeystoreFormat,
            onSubmit = { alias, pass, storePass, format ->
                coroutineScope.launch {
                    val result = importExportViewModel.tryKeystoreImport(alias, pass, storePass, format)
                    if (!result) {
                        context.toast(wrongCredentialsText)
                    }
                }
            }
        )
    }

    if (showInstallerDialog.value) {
        InstallerSelectionDialogContainer(
            settingsViewModel = settingsViewModel,
            onDismiss = { showInstallerDialog.value = false }
        )
    }

    if (showChangelogDialog.value) {
        ManagerChangelogDialog(onDismiss = { showChangelogDialog.value = false })
    }

    pendingSettingsImportUri?.let { uri ->
        ImportModeDialog(
            titleRes = R.string.settings_system_import_manager_settings_mode_title,
            descriptionRes = R.string.settings_system_import_manager_settings_mode_description,
            onDismiss = { pendingSettingsImportUri = null },
            onSelect = { mode ->
                importExportViewModel.importManagerSettings(uri, mode)
                pendingSettingsImportUri = null
            }
        )
    }

    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val backLabel = stringResource(R.string.back)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Invisible back button for TalkBack
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .semantics {
                    contentDescription = backLabel
                    onClick(action = { backPressedDispatcher?.onBackPressed(); true })
                }
        )

        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.material3.IconButton(
                onClick = { backPressedDispatcher?.onBackPressed() },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = backLabel,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
            }
            androidx.compose.material3.Text(
                text = stringResource(R.string.settings),
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        }

        // TADa Manager Brand Banner
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(app.tada.manager.R.drawable.tada_mascot),
                    contentDescription = "TADa Mascot",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    androidx.compose.material3.Text(
                        text = "TADa Manager",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    androidx.compose.material3.Text(
                        text = app.tada.manager.BuildConfig.VERSION_NAME,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Main Scrolling Content
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollFade(mainScrollState)
                    .verticalScroll(mainScrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Category: Appearance
                SettingsCategoryCard(titleRes = R.string.appearance) {
                    AppearanceTabContent(
                        theme = theme,
                        themeStyle = themeStyle,
                        pureBlackTheme = pureBlackTheme,
                        customAccentColorHex = customAccentColorHex,
                        themeViewModel = themeViewModel,
                        onThemeSelectorPositioned = { globalOnboardingState?.themeSelectorBounds = it },
                        onThemeSelectorScrollTarget = { themeSelectorScrollTarget = it }
                    )
                }

                // Category: Advanced
                SettingsCategoryCard(titleRes = R.string.advanced) {
                    AdvancedTabContent(
                        patchOptionsViewModel = patchOptionsViewModel,
                        homeViewModel = homeViewModel,
                        settingsViewModel = settingsViewModel,
                        onExpertModeItemPositioned = { globalOnboardingState?.expertModeBounds = it },
                        onExpertModeScrollTarget = { expertModeScrollTarget = it },
                        onProcessRuntimePositioned = { globalOnboardingState?.processRuntimeBounds = it },
                        onProcessRuntimeScrollTarget = { processRuntimeScrollTarget = it }
                    )
                }

                // Category: System
                SettingsCategoryCard(titleRes = R.string.system) {
                    SystemTabContent(
                        settingsViewModel = settingsViewModel,
                        onShowInstallerDialog = { showInstallerDialog.value = true },
                        importExportViewModel = importExportViewModel,
                        onImportKeystore = { importKeystoreLauncher() },
                        onExportKeystore = {
                            if (isTV) importExportViewModel.exportKeystoreToDownloads()
                            else exportKeystoreLauncher.launch("TADa.keystore")
                        },
                        onImportSettings = { importSettingsLauncher() },
                        onExportSettings = {
                            if (isTV) importExportViewModel.exportManagerSettingsToDownloads()
                            else exportSettingsLauncher.launch("tada_manager_settings.json")
                        },
                        onExportDebugLogs = {
                            if (isTV) importExportViewModel.exportDebugLogsToDownloads()
                            else exportDebugLogsLauncher.launch(importExportViewModel.debugLogFileName)
                        },
                        onAboutClick = { showAboutDialog.value = true },
                        onChangelogClick = { showChangelogDialog.value = true },
                        onStartTour = onStartTour,
                        onInstallerSectionPositioned = { globalOnboardingState?.installerSectionBounds = it },
                        onInstallerScrollTarget = { installerScrollTarget = it },
                        onFilePickerPositioned = { globalOnboardingState?.filePickerBounds = it }
                    )
                }

                // Add bottom padding to allow scrolling past bottom nav in edge-to-edge
                Spacer(modifier = Modifier.height(100.dp))
            }
            ListScrollbar(scrollState = mainScrollState)
        }
    }
}

/** Wrapper Card for Settings Categories */
@Composable
fun SettingsCategoryCard(
    titleRes: Int,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.material3.Text(
            text = stringResource(titleRes),
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Box(modifier = Modifier.padding(vertical = 12.dp)) {
                content()
            }
        }
    }
}
