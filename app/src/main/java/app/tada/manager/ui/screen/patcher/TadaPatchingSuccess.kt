package app.tada.manager.ui.screen.patcher

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tada.manager.R
import app.tada.manager.ui.viewmodel.InstallViewModel

@Composable
fun TadaPatchingSuccess(
    packageName: String,
    appName: String = packageName,
    patchCount: Int,
    installState: InstallViewModel.InstallState = InstallViewModel.InstallState.Ready,
    onInstall: () -> Unit,
    onOpen: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    isSaving: Boolean = false,
    onHomeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Gấu trúc ăn mừng
        Image(
            painter = painterResource(id = R.drawable.tada_mascot_celebrate),
            contentDescription = "Success Mascot",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tiêu đề
        Text(
            text = if (installState is InstallViewModel.InstallState.Installed) "Đã cài đặt! 🎉" else "Xong rồi! 🎉",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = if (installState is InstallViewModel.InstallState.Installed)
                "$appName đã được cài đặt thành công!"
            else
                "$appName đã được vá với $patchCount bản vá",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Thẻ thông tin trạng thái APK
        val (statusText, statusColor) = when (installState) {
            is InstallViewModel.InstallState.Installing -> "Đang cài đặt..." to MaterialTheme.colorScheme.primary
            is InstallViewModel.InstallState.Installed -> "Đã cài đặt" to Color(0xFF4CAF50)
            is InstallViewModel.InstallState.Conflict -> "Xung đột chữ ký" to Color(0xFFFFA000)
            is InstallViewModel.InstallState.Error -> "Lỗi cài đặt" to MaterialTheme.colorScheme.error
            else -> "Chờ cài đặt" to MaterialTheme.colorScheme.primary
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "patched_$packageName.apk",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Nút hành động chính
        when (installState) {
            is InstallViewModel.InstallState.Installed -> {
                Button(
                    onClick = onOpen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Outlined.Launch, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mở ứng dụng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            is InstallViewModel.InstallState.Installing -> {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Đang cài đặt...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is InstallViewModel.InstallState.Conflict -> {
                Button(
                    onClick = onInstall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Giải quyết xung đột & Cài đặt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            else -> {
                Button(
                    onClick = onInstall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (installState is InstallViewModel.InstallState.Error) "Thử cài đặt lại" else "Cài đặt ngay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nút Lưu APK vào máy
        OutlinedButton(
            onClick = onSaveClick,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đang lưu...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lưu APK vào máy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nút Về màn hình chính / Để sau
        TextButton(
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                text = if (installState is InstallViewModel.InstallState.Installed) "Về màn hình chính" else "Để sau",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
