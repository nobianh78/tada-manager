import re

with open("app/src/main/java/app/tada/manager/ui/screen/home/HomeAppCards.kt", "r") as f:
    content = f.read()

# Replace homeAppCardStyle
new_style = """private data class HomeAppCardStyle(
    val iconSize: Dp = 48.dp,
    val titleStyle: TextStyle,
    val subtitleStyle: TextStyle
)

@Composable
private fun homeAppCardStyle(): HomeAppCardStyle {
    return HomeAppCardStyle(
        titleStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        subtitleStyle = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
    )
}
"""
content = re.sub(r"private data class HomeAppCardStyle.*?\}", new_style, content, flags=re.DOTALL)

with open("app/src/main/java/app/tada/manager/ui/screen/home/HomeAppCards.kt", "w") as f:
    f.write(content)
