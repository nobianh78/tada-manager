package app.tada.manager.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.tada.manager.domain.installer.InstallerFileProvider
import app.tada.manager.network.api.TadaAPI
import app.tada.manager.network.dto.GitHubRelease
import app.tada.manager.network.service.AssetDownloader
import app.tada.manager.network.utils.APIResponse
import app.tada.manager.util.APK_MIMETYPE
import io.ktor.client.request.url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class HomeResourcesViewModel(
    private val app: Application,
    private val assetDownloader: AssetDownloader,
    private val api: TadaAPI
) : AndroidViewModel(app) {

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates.asStateFlow()

    fun downloadAndInstall(resourceId: String, repoOwner: String, repoName: String, assetRegex: Regex) {
        viewModelScope.launch {
            try {
                updateState(resourceId, DownloadState.Downloading(0f))

                val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
                val releaseResp = api.client.request<GitHubRelease> { url(url) }
                val release = if (releaseResp is APIResponse.Success) {
                    releaseResp.data
                } else {
                    throw Exception("Failed to fetch release")
                }
                
                val asset = release.assets.firstOrNull { assetRegex.matches(it.name) }
                    ?: throw Exception("No asset found matching regex")

                val downloadUrl = asset.downloadUrl ?: asset.url
                if (downloadUrl == null) throw Exception("No valid download URL")
                
                val saveFile = File(app.cacheDir, "${InstallerFileProvider.SHARE_DIR}/${asset.name}")
                saveFile.parentFile?.mkdirs()

                assetDownloader.downloadToFile(downloadUrl, saveFile) { read, total ->
                    val progress = if (total != null && total > 0) read.toFloat() / total else 0f
                    updateState(resourceId, DownloadState.Downloading(progress))
                }

                updateState(resourceId, DownloadState.Idle)
                installApk(saveFile)

            } catch (e: Exception) {
                Log.e("HomeResourcesViewModel", "Failed to download $resourceId", e)
                updateState(resourceId, DownloadState.Idle)
            }
        }
    }

    private fun installApk(file: File) {
        val uri = InstallerFileProvider.getUriForFile(app, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, APK_MIMETYPE)
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or 
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }
        app.startActivity(intent)
    }

    fun cancelDownload(resourceId: String) {
        // Just reset state for now
        updateState(resourceId, DownloadState.Idle)
    }

    private fun updateState(id: String, state: DownloadState) {
        _downloadStates.value = _downloadStates.value.toMutableMap().apply { put(id, state) }
    }

    sealed class DownloadState {
        object Idle : DownloadState()
        data class Downloading(val progress: Float) : DownloadState()
    }
}
