package app.tada.manager.ui.model

import androidx.annotation.StringRes

data class ResourceItem(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int,
    val packageName: String,
    val repoOwner: String,
    val repoName: String,
    val assetRegex: Regex,
    val iconRes: Int? = null
)
