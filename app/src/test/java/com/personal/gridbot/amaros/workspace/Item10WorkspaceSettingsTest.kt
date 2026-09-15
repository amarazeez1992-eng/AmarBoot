package com.personal.gridbot.amaros.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Item10WorkspaceSettingsTest {
    @Test
    fun settingsExposeOpenLimitedAndOffCapabilityControls() {
        val settings = AmarWorkspaceSettings(
            multimodalAccess = AmarFeatureAccess.LIMITED,
            videoAccess = AmarFeatureAccess.OFF,
            allowExport = AmarFeatureAccess.OPEN
        )
        assertEquals(AmarFeatureAccess.LIMITED, settings.multimodalAccess)
        assertEquals(AmarFeatureAccess.OFF, settings.videoAccess)
        assertEquals(AmarFeatureAccess.OPEN, settings.allowExport)
    }

    @Test
    fun osPermissionAlwaysOverridesCameraScreenAndControlPreferences() {
        val settings = AmarWorkspaceSettings(
            cameraAccess = AmarCameraAccess.ALLOWED,
            screenAccess = AmarScreenAccess.ALLOWED,
            deviceControlAccess = AmarDeviceControlAccess.ALLOWED
        )
        assertEquals(AmarCameraAccess.OFF, settings.effectiveCamera(false))
        assertEquals(AmarScreenAccess.OFF, settings.effectiveScreen(false))
        assertEquals(AmarDeviceControlAccess.OFF, settings.effectiveDeviceControl(false))
        assertEquals(AmarCameraAccess.ALLOWED, settings.effectiveCamera(true))
    }

    @Test
    fun sensitiveActionsRemainFailClosedWithoutConfirmation() {
        val settings = AmarWorkspaceSettings(
            sensitiveActionPolicy = AmarSensitiveActionPolicy.ALWAYS_CONFIRM
        )
        assertFalse(settings.effectiveSensitiveActionAllowed(true, false))
        assertTrue(settings.effectiveSensitiveActionAllowed(true, true))
        assertFalse(settings.effectiveSensitiveActionAllowed(false, true))
    }

    @Test
    fun settingsStoreReplacesTheCompleteSnapshot() {
        val store = AmarWorkspaceSettingsStore()
        val updated = AmarWorkspaceSettings(
            webAccess = AmarWebAccess.OPEN,
            answerDetail = AmarAnswerDetail.DETAILED,
            background = AmarBackgroundMode.DYNAMIC,
            theme = AmarThemeMode.DARK,
            fontSizeSp = 20f,
            uiScale = 1.2f
        )
        assertEquals(updated, store.replace(updated))
        assertEquals(updated, store.read())
    }

    @Test
    fun presentationSettingsRemainUserSelectable() {
        val settings = AmarWorkspaceSettings(
            theme = AmarThemeMode.DARK,
            background = AmarBackgroundMode.DYNAMIC,
            backgroundId = "holographic-01",
            accentColorArgb = 0xFF00E5FF,
            textColorArgb = 0xFFFFFFFF,
            fontFamily = "Noto Sans Arabic",
            fontSizeSp = 18f,
            fontWeight = AmarFontWeight.BOLD,
            uiScale = 1.1f
        )
        assertEquals(AmarBackgroundMode.DYNAMIC, settings.background)
        assertEquals("holographic-01", settings.backgroundId)
        assertEquals("Noto Sans Arabic", settings.fontFamily)
        assertEquals(18f, settings.fontSizeSp, 0f)
    }
}
