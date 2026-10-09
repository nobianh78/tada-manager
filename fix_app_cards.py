import re

with open("app/src/main/java/app/tada/manager/ui/screen/home/HomeAppCards.kt", "r") as f:
    content = f.read()

# Fix combinedClickable
content = content.replace(".androidx.compose.foundation.combinedClickable(", ".combinedClickable(")

# Fix AppDataSource
content = content.replace("app.tada.manager.ui.screen.shared.AppDataSource.INSTALLED", "app.tada.manager.ui.screen.shared.AppDataSource.INSTALLED")
# Wait, let's just import it at the top if it's missing, but it is fully qualified.
# Kotlin doesn't allow fully qualified enum entries if the enum class itself isn't imported sometimes, or maybe it does. Let's change it to just AppDataSource.INSTALLED and add import.
content = content.replace("app.tada.manager.ui.screen.shared.AppDataSource.INSTALLED", "AppDataSource.INSTALLED")
content = content.replace("app.tada.manager.ui.screen.shared.AppDataSource.PATCHED_APK", "AppDataSource.PATCHED_APK")

if "import app.tada.manager.ui.screen.shared.AppDataSource" not in content:
    content = content.replace("package app.tada.manager.ui.screen.home", "package app.tada.manager.ui.screen.home\n\nimport app.tada.manager.ui.screen.shared.AppDataSource")

# Fix fully qualified shapes and buttons
content = content.replace("androidx.compose.foundation.shape.CircleShape", "CircleShape")
content = content.replace("androidx.compose.foundation.BorderStroke", "BorderStroke")
content = content.replace("androidx.compose.material3.OutlinedButton", "OutlinedButton")
content = content.replace("androidx.compose.material3.ButtonDefaults.outlinedButtonColors", "ButtonDefaults.outlinedButtonColors")

if "import androidx.compose.foundation.shape.CircleShape" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.shape.CircleShape\nimport androidx.compose.foundation.BorderStroke\nimport androidx.compose.material3.OutlinedButton\nimport androidx.compose.material3.ButtonDefaults")

with open("app/src/main/java/app/tada/manager/ui/screen/home/HomeAppCards.kt", "w") as f:
    f.write(content)
