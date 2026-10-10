package app.tada.manager.ui.screen.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tada.manager.ui.screen.shared.ActionPillButton
import app.tada.manager.ui.screen.shared.ActionPillColors
import app.tada.manager.ui.screen.shared.Defaults

@Composable
fun FirstLaunchPermissionsScreen(onComplete: () -> Unit) {
    val permissionsToRequest = remember {
        val list = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        list
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onComplete()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Chào mừng đến với TADa",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Để ứng dụng hoạt động tốt nhất, vui lòng cấp quyền truy cập bộ nhớ và thông báo. Sau khi cấp quyền, chúng ta sẽ bắt đầu khám phá các tính năng!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            ActionPillButton(
                onClick = {
                    if (permissionsToRequest.isNotEmpty()) {
                        launcher.launch(permissionsToRequest.toTypedArray())
                    } else {
                        onComplete()
                    }
                },
                icon = Icons.Outlined.Check,
                label = "Cấp quyền",
                contentDescription = "Cấp quyền",
                colors = ActionPillColors.primary(),
                large = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
