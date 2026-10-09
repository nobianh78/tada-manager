package app.tada.manager.ui.screen.home

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tada.manager.patcher.patch.PatchBundleInfo
import app.tada.manager.patcher.patch.PatchInfo
import app.tada.manager.patcher.patch.PatchLockState
import app.tada.manager.util.Options
import app.tada.manager.util.PatchSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TadaExpertModeDialog(
    packageName: String,
    appName: String? = null,
    appIcon: Drawable? = null,
    newPatches: Map<Int, Set<String>> = emptyMap(),
    options: Options = emptyMap(),
    allPatchesInfo: List<Pair<PatchBundleInfo.Scoped, List<Pair<PatchInfo, Boolean>>>>,
    totalSelectedCount: Int,
    totalPatchesCount: Int,
    hasMultipleBundles: Boolean,
    patchActions: ExpertPatchActions,
    savedPatches: PatchSelection = emptyMap(),
    lockStateOf: (PatchInfo) -> PatchLockState = { PatchLockState.NONE },
    holdsUniversalPatches: (bundleUid: Int, patches: List<Pair<PatchInfo, Boolean>>) -> Boolean = { _, _ -> false },
    proceedText: String = "Vá",
    warnOnMultipleBundles: Boolean = true,
    prereleaseBundleUids: Set<Int> = emptySet(),
    hiddenSourceCount: Int = 0,
    onShowHiddenSources: () -> Unit = {},
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    val title = appName ?: packageName
    val version = allPatchesInfo.firstOrNull()?.first?.version ?: ""
    val subtitle = "$totalPatchesCount bản vá • v$version"

    var searchQuery by remember { mutableStateOf("") }
    val flatPatches = remember(allPatchesInfo, searchQuery) {
        allPatchesInfo.flatMap { (bundle, patches) ->
            patches.map { bundle.uid to it }
        }.filter { (_, patchPair) ->
            searchQuery.isBlank() || patchPair.first.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface // Nền kem sáng
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Thanh tìm kiếm
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                placeholder = { Text("Tìm bản vá…") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Danh sách bản vá
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(flatPatches, key = { it.second.first.name }) { (bundleUid, patchPair) ->
                    val (patch, isEnabled) = patchPair
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { patchActions.onPatchToggle(bundleUid, patch.name) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = patch.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = patch.description ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { patchActions.onPatchToggle(bundleUid, patch.name) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                }
            }

            // Nút pill cam to "Vá ngay • X đã chọn"
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = totalSelectedCount > 0
            ) {
                Text(
                    text = "Vá ngay • $totalSelectedCount đã chọn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
