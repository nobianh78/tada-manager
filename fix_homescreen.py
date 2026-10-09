import re

with open("app/src/main/java/app/tada/manager/ui/screen/HomeScreen.kt", "r") as f:
    content = f.read()

# Add extraction of patchVersion
patch_version_logic = """    val sources by homeViewModel.patchBundleRepository.sources.collectAsStateWithLifecycle(emptyList())
    val patchVersion = sources.firstOrNull { it.enabled }?.version ?: "1.0.0"
    
    PullToRefreshBox"""
content = content.replace("    PullToRefreshBox", patch_version_logic)

# Pass patchVersion to SectionsLayout
content = content.replace("SectionsLayout(\n                notifications", "SectionsLayout(\n                patchVersion = patchVersion,\n                notifications")

with open("app/src/main/java/app/tada/manager/ui/screen/HomeScreen.kt", "w") as f:
    f.write(content)
