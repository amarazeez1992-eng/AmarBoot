package com.personal.gridbot.amaros.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Item10EditorPreferencesTest {
    @Test
    fun sizeIsClampedToConfiguredBounds() {
        val size = AmarWorkspaceElementSize("answer", 5000f, 1f).normalized()
        assertEquals(2000f, size.widthDp, 0.01f)
        assertEquals(48f, size.heightDp, 0.01f)
    }

    @Test
    fun layoutCanResizeAnExistingElement() {
        val layout = AmarWorkspaceLayoutPreferences(listOf(AmarWorkspaceElementSize("answer", 400f, 300f)))
        val resized = layout.resize("answer", 700f, 500f)
        assertEquals(700f, resized.elements.single().widthDp, 0.01f)
        assertEquals(500f, resized.elements.single().heightDp, 0.01f)
    }

    @Test
    fun notebookSupportsSaveReadAndDelete() {
        val store = AmarNotebookStore()
        val page = AmarNotebookPage("n1", "Notes", "copy/paste test", 1L, 2L)
        assertTrue(store.save(page))
        assertEquals("copy/paste test", store.page("n1")?.text)
        assertTrue(store.delete("n1"))
        assertEquals(null, store.page("n1"))
    }

    @Test
    fun editorPreferencesExposeColorSizeWeightAndClipboardControls() {
        val prefs = AmarEditorPreferences(
            textColorArgb = 0xFF00FF00,
            fontSizeSp = 20f,
            fontWeight = AmarFontWeight.BOLD,
            allowCopy = true,
            allowPaste = true
        )
        assertEquals(0xFF00FF00, prefs.textColorArgb)
        assertEquals(20f, prefs.fontSizeSp, 0.01f)
        assertEquals(AmarFontWeight.BOLD, prefs.fontWeight)
        assertTrue(prefs.allowCopy && prefs.allowPaste)
    }
}
