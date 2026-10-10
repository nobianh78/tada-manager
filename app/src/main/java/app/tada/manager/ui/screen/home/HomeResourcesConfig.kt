package app.tada.manager.ui.screen.home

import app.tada.manager.R
import app.tada.manager.ui.model.ResourceItem

val HomeResourcesConfig = listOf(
    ResourceItem(
        id = "microg",
        nameRes = R.string.resource_microg_name,
        descRes = R.string.resource_microg_desc,
        packageName = "app.revanced.android.gms",
        repoOwner = "nobianh78",
        repoName = "tada-microg",
        assetRegex = Regex("^microg-[\\d\\.]+\\.apk$"),
        iconRes = null // We'll just use a default icon if this is null
    )
)
