import re

path = "app/src/main/java/app/tada/manager/ui/screen/home/HomeAppsSectionList.kt"
with open(path, "r") as f:
    content = f.read()

# Replace groupedAppCards items(...)
grouped_items_pattern = r"""(items\(\s*items = group\.items,\s*key = { item -> "category_\$\{group\.id \?: "uncategorized"\}_\$\{item\.id\}" }\s*\) \{ item ->)\s*(.*?)(?=\s*\}\s*\})"""

# We'll replace it with chunked items
new_grouped_items = """items(
                items = group.items.chunked(2),
                key = { chunk -> "category_${group.id ?: "uncategorized"}_${chunk.first().id}" }
            ) { chunkItems ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    chunkItems.forEach { item ->
                        val groupKey = group.selectionKey()
                        val isSelected = selectedPackages.contains(item.id) &&
                                (state.selectedGroupKey == null || state.selectedGroupKey == groupKey)
                        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                            DynamicAppCard(
                                item = item,
                                onAppClick = {
                                    if (state.isMultiSelectMode) {
                                        state.toggleInGroup(item.id, groupKey)
                                    } else {
                                        appActions.onAppClick(item)
                                    }
                                },
                                onHide = { appActions.onHideApp(item.id) },
                                onShowPatches = { appActions.onShowPatches(item) },
                                showGestureHint = item.id == firstFilteredPackage && showGestureHint,
                                onGestureHintShown = appActions.onGestureHintShown,
                                isSelected = isSelected,
                                isMultiSelectMode = state.isMultiSelectMode,
                                onLongPress = {
                                    if (!state.isCategoryBarVisible) {
                                        state.isMultiSelectMode = true
                                        state.toggleInGroup(item.id, groupKey)
                                    }
                                }
                            )
                        }
                    }
                    if (chunkItems.size == 1) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                    }
                }"""
content = re.sub(grouped_items_pattern, new_grouped_items, content, flags=re.DOTALL)

# Replace flatAppCards itemsIndexed(...)
flat_items_pattern = r"""(itemsIndexed\(\s*items = items,\s*key = { _, item -> item\.id }\s*\) \{ index, item ->)\s*(.*?)(?=\s*\}\s*\})"""

new_flat_items = """itemsIndexed(
        items = items.chunked(2),
        key = { _, chunk -> chunk.first().id }
    ) { chunkIndex, chunkItems ->
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().animateItem(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            chunkItems.forEachIndexed { itemIndex, item ->
                val index = chunkIndex * 2 + itemIndex
                // Moves the card one step and reports where it landed, so a screen reader user hears
                // the result of an action they cannot see
                fun moveBy(offset: Int, announcePosition: Int) {
                    val current = state.localOrder.toMutableList()
                    val from = current.indexOf(item.id)
                    val target = from + offset
                    if (from < 0 || target !in current.indices) return
                    val moved = current.removeAt(from)
                    current.add(target, moved)
                    state.localOrder = current
                    appActions.onSaveOrder(current)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onMoveAnnouncement(
                        moveAnnouncementFormat.format(item.displayName, from + announcePosition, current.size)
                    )
                }

                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                    DynamicAppCard(
                        item = item,
                        onAppClick = {
                            if (state.isMultiSelectMode) {
                                selectedPackages.toggle(item.id)
                            } else {
                                appActions.onAppClick(item)
                            }
                        },
                        onHide = { appActions.onHideApp(item.id) },
                        onShowPatches = { appActions.onShowPatches(item) },
                        showGestureHint = index == 0 && showGestureHint,
                        onGestureHintShown = appActions.onGestureHintShown,
                        isSelected = selectedPackages.contains(item.id),
                        isMultiSelectMode = state.isMultiSelectMode,
                        onLongPress = {
                            state.isMultiSelectMode = true
                            selectedPackages.toggle(item.id)
                        },
                        onMoveUp = if (directReorderAllowed && index > 0) {
                            { moveBy(offset = -1, announcePosition = 0) }
                        } else null,
                        onMoveDown = if (directReorderAllowed && index < items.size - 1) {
                            { moveBy(offset = 1, announcePosition = 2) }
                        } else null,
                        modifier = if (index == 0 && onboardingState != null)
                            Modifier.onGloballyPositioned { coords ->
                                onboardingState.firstAppCardBounds = coords.boundsInWindow()
                            }
                        else Modifier
                    )
                }
            }
            if (chunkItems.size == 1) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            }
        }"""
content = re.sub(flat_items_pattern, new_flat_items, content, flags=re.DOTALL)

with open(path, "w") as f:
    f.write(content)
