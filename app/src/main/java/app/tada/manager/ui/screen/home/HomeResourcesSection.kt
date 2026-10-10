package app.tada.manager.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tada.manager.R
import app.tada.manager.ui.model.ResourceItem
import app.tada.manager.ui.screen.shared.Defaults
import app.tada.manager.ui.screen.shared.SemanticTone
import app.tada.manager.ui.viewmodel.HomeResourcesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeResourcesSection(
    modifier: Modifier = Modifier,
    viewModel: HomeResourcesViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    // Check install state for each resource dynamically
    // Simple LaunchedEffect or just re-checking on composition is fine, but to be safe:
    var installedStates by remember { mutableStateOf(mapOf<String, Boolean>()) }
    
    LaunchedEffect(Unit, viewModel.downloadStates) {
        val states = mutableMapOf<String, Boolean>()
        for (item in HomeResourcesConfig) {
            val installed = try {
                packageManager.getPackageInfo(item.packageName, 0)
                true
            } catch (e: Exception) {
                false
            }
            states[item.id] = installed
        }
        installedStates = states
    }

    val downloadStates by viewModel.downloadStates.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Defaults.ContentPaddingExpanded)
            .padding(top = Defaults.ContentPaddingExpanded, bottom = Defaults.ContentPaddingMedium)
    ) {
        Text(
            text = stringResource(R.string.home_resources_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Defaults.ItemSpacing)
        )

        HomeResourcesConfig.forEach { item ->
            val isInstalled = installedStates[item.id] == true
            val downloadState = downloadStates[item.id] ?: HomeResourcesViewModel.DownloadState.Idle
            
            ResourceCard(
                item = item,
                isInstalled = isInstalled,
                downloadState = downloadState,
                onDownload = {
                    viewModel.downloadAndInstall(
                        resourceId = item.id,
                        repoOwner = item.repoOwner,
                        repoName = item.repoName,
                        assetRegex = item.assetRegex
                    )
                },
                onCancel = {
                    viewModel.cancelDownload(item.id)
                }
            )
            Spacer(modifier = Modifier.height(Defaults.ItemSpacing))
        }
    }
}

@Composable
private fun ResourceCard(
    item: ResourceItem,
    isInstalled: Boolean,
    downloadState: HomeResourcesViewModel.DownloadState,
    onDownload: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Defaults.ContentPaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (item.iconRes != null) {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(Defaults.ContentPaddingMedium))

            // Text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(item.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(item.descRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(Defaults.ContentPaddingMedium))

            // Action Button
            when {
                isInstalled -> {
                    FilledTonalButton(
                        onClick = { },
                        enabled = false,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.resource_action_installed))
                    }
                }
                downloadState is HomeResourcesViewModel.DownloadState.Downloading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            progress = if (downloadState.progress > 0) downloadState.progress else 0f,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = SemanticTone.Warning.container // brand orange-like
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SemanticTone.Warning.container, // Cam brand
                            contentColor = SemanticTone.Warning.content
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.resource_action_download))
                    }
                }
            }
        }
    }
}
