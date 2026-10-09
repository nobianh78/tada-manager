import re

with open("app/src/main/java/app/tada/manager/ui/screen/home/HomeSectionsLayout.kt", "r") as f:
    content = f.read()

header_code = """
                            // 1. Top Row
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (chromeFlags.showSearchButton) {
                                    androidx.compose.material3.IconButton(onClick = searchState.onToggle) { androidx.compose.material3.Icon(Icons.Outlined.Search, "Search") }
                                }
                                if (chromeFlags.showSortButton) {
                                    androidx.compose.material3.IconButton(onClick = onSortClick) { androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.Sort, "Sort") }
                                }
                                androidx.compose.material3.IconButton(onClick = chromeActions.onSettingsClick) { androidx.compose.material3.Icon(Icons.Outlined.Settings, "Settings") }
                            }

                            // 2. Banner
                            androidx.compose.material3.Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(androidx.compose.ui.graphics.Color(0xFFFFB74D), androidx.compose.ui.graphics.Color(0xFFF57C00))
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(app.tada.manager.R.drawable.tada_mascot_wave),
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp)
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Column {
                                            androidx.compose.material3.Text("Vá app thôi!", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color.White)
                                            androidx.compose.material3.Text("${apps.visible.size} app sẵn sàng", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f))
                                        }
                                    }
                                }
                            }

                            // 3. Section Title
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                                androidx.compose.material3.Text(
                                    text = "App của bạn",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
"""

# Replace in landscape
p_landscape = r"(if \(!greetingMessage\.isNullOrEmpty\(\)\) \{\s*GreetingSection\(\s*message = greetingMessage,\s*modifier = Modifier\.widthIn\(max = maxCardWidth\)\.fillMaxWidth\(\),\s*onRefresh = chromeActions\.onRefreshGreeting\s*\)\s*Spacer\(modifier = Modifier\.height\(itemSpacing\)\)\s*\})"
content = re.sub(p_landscape, header_code.strip(), content)

# Replace in portrait
p_portrait = r"(if \(!greetingMessage\.isNullOrEmpty\(\)\) \{\s*GreetingSection\(\s*message = greetingMessage,\s*modifier = Modifier\.padding\(horizontal = contentPadding\),\s*onRefresh = chromeActions\.onRefreshGreeting\s*\)\s*Spacer\(modifier = Modifier\.height\(itemSpacing\)\)\s*\} else if \(isGroupedAppView\) \{\s*Spacer\(modifier = Modifier\.height\(24\.dp\)\)\s*\})"
content = re.sub(p_portrait, header_code.strip(), content)

with open("app/src/main/java/app/tada/manager/ui/screen/home/HomeSectionsLayout.kt", "w") as f:
    f.write(content)
