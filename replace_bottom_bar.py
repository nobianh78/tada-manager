import re

with open("app/src/main/java/app/tada/manager/ui/screen/home/HomeSectionsLayout.kt", "r") as f:
    content = f.read()

# Replace SectionsLayout signature
content = content.replace("fun SectionsLayout(\n    notifications", "fun SectionsLayout(\n    patchVersion: String,\n    notifications")

# Replace HomeBottomActionBar call in portrait
p_bottom = r"if \(!isLandscape\(\)\) \{\s*HomeBottomActionBar\([\s\S]*?\)\s*\}"
new_bottom = """if (!isLandscape()) {
                // Bottom Pill
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(androidx.compose.ui.graphics.Color.Green, androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.Text(
                                text = "TADa Patches v${patchVersion} • đã cập nhật",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }"""
content = re.sub(p_bottom, new_bottom, content, count=1)

with open("app/src/main/java/app/tada/manager/ui/screen/home/HomeSectionsLayout.kt", "w") as f:
    f.write(content)
