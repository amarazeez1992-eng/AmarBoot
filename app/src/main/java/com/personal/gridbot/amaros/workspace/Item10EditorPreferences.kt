package com.personal.gridbot.amaros.workspace

/** User-controlled presentation settings; UI implementation remains outside Item 10 scope. */
data class AmarEditorPreferences(
    val textColorArgb: Long = 0xFFFFFFFF,
    val fontSizeSp: Float = 16f,
    val fontWeight: AmarFontWeight = AmarFontWeight.NORMAL,
    val showColorPalette: Boolean = true,
    val allowCopy: Boolean = true,
    val allowPaste: Boolean = true,
    val showNotebook: Boolean = true
)

enum class AmarFontWeight { LIGHT, NORMAL, MEDIUM, BOLD }

data class AmarWorkspaceElementSize(
    val elementId: String,
    val widthDp: Float,
    val heightDp: Float,
    val minWidthDp: Float = 48f,
    val minHeightDp: Float = 48f,
    val maxWidthDp: Float = 2000f,
    val maxHeightDp: Float = 2000f
) {
    init {
        require(minWidthDp > 0f && minHeightDp > 0f)
        require(maxWidthDp >= minWidthDp && maxHeightDp >= minHeightDp)
    }

    fun normalized(): AmarWorkspaceElementSize = copy(
        widthDp = widthDp.coerceIn(minWidthDp, maxWidthDp),
        heightDp = heightDp.coerceIn(minHeightDp, maxHeightDp)
    )
}

data class AmarWorkspaceLayoutPreferences(
    val elements: List<AmarWorkspaceElementSize> = emptyList()
) {
    fun resize(elementId: String, widthDp: Float, heightDp: Float): AmarWorkspaceLayoutPreferences =
        copy(elements = elements.map { element ->
            if (element.elementId == elementId) element.copy(widthDp = widthDp, heightDp = heightDp).normalized()
            else element
        })
}

/** Internal notebook model for notes, copying and pasting; persistence is owned by the workspace store. */
data class AmarNotebookPage(
    val id: String,
    val title: String,
    val text: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

class AmarNotebookStore {
    private val pages = linkedMapOf<String, AmarNotebookPage>()

    fun save(page: AmarNotebookPage): Boolean {
        if (page.id.isBlank() || page.title.isBlank()) return false
        pages[page.id] = page
        return true
    }

    fun page(id: String): AmarNotebookPage? = pages[id]
    fun pages(): List<AmarNotebookPage> = pages.values.toList()
    fun delete(id: String): Boolean = pages.remove(id) != null
}
