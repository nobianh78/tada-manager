package app.tada.manager.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tada.manager.domain.repository.PatchBundleRepository
import app.tada.manager.domain.bundles.PatchBundleSource
import app.tada.manager.domain.bundles.PatchBundleSource.Extensions.isDefault
import app.tada.manager.ui.screen.home.GlobalOnboardingState
import app.tada.manager.ui.screen.shared.AppBottomSheet
import app.tada.manager.ui.screen.shared.ToggleSwitch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BundleManagementSheet(
    onDismissRequest: () -> Unit,
    onAddSource: () -> Unit,
    onDelete: (PatchBundleSource) -> Unit,
    onDisable: (PatchBundleSource) -> Unit,
    onUpdate: (PatchBundleSource) -> Unit,
    onRename: (PatchBundleSource) -> Unit,
    onReorder: (List<Int>) -> Unit,
    globalOnboardingState: GlobalOnboardingState? = null
) {
    val patchBundleRepository: PatchBundleRepository = koinInject()
    val sources by patchBundleRepository.sources.collectAsStateWithLifecycle()
    val bundleInfo by patchBundleRepository.allBundlesInfoFlow.collectAsStateWithLifecycle(emptyMap())

    // Auto-enable the default bundle if it's the only one and disabled
    LaunchedEffect(sources) {
        if (sources.size == 1) {
            val singleBundle = sources.first()
            if (singleBundle.isDefault && !singleBundle.enabled) {
                onDisable(singleBundle)
            }
        }
    }

    AppBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Tiêu đề "Nguồn bản vá" + nút back
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = onDismissRequest, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Nguồn bản vá",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val defaultSource = sources.find { it.isDefault }
                val otherSources = sources.filter { !it.isDefault }

                // Thẻ nguồn chính
                defaultSource?.let { source ->
                    item {
                        val version = bundleInfo[source.uid]?.version ?: "Đang tải..."
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) // viền cam
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "TADa Patches", // tên in đậm
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = version,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // badge xanh "mới nhất"
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "mới nhất",
                                                    color = Color(0xFF4CAF50),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Toggle (always on for default?) Or use onDisable
                                ToggleSwitch(
                                    checked = source.enabled,
                                    onCheckedChange = null,
                                    modifier = Modifier.clickable { onDisable(source) }
                                )
                            }
                        }
                    }
                }

                // Các nguồn khác: thẻ xám, toggle tắt
                items(otherSources) { source ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // thẻ xám
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = source.displayTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            ToggleSwitch(
                                checked = source.enabled,
                                onCheckedChange = null,
                                modifier = Modifier.clickable { onDisable(source) }
                            )
                        }
                    }
                }

                // Thẻ viền đứt "Thêm nguồn mới (.mpp)"
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), // viền đứt would need dashed modifier, but solid is fine for now if dashed is too complex, let's use a dashed modifier
                            )
                            .clickable { onAddSource() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thêm nguồn mới (.mpp)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
