package com.personal.gridbot.amaros.workspace

/**
 * User-owned policy for Item 10. Presentation and capability preferences are explicit,
 * persisted by the host application, and never grant permissions the OS has not granted.
 */
enum class AmarFeatureAccess { OPEN, LIMITED, OFF }

enum class AmarWebAccess { RESTRICTED, OPEN }

enum class AmarAnswerDetail { SHORT, DETAILED }

enum class AmarBackgroundMode { STATIC, DYNAMIC, AUTOMATIC }

enum class AmarThemeMode { LIGHT, DARK, SYSTEM }

enum class AmarCameraAccess { ASK_EACH_SESSION, ALLOWED, OFF }

enum class AmarScreenAccess { ASK_EACH_SESSION, ALLOWED, OFF }

enum class AmarDeviceControlAccess { ASK_EACH_ACTION, ALLOWED, OFF }

enum class AmarMemoryAccess { ASK_TO_SAVE, AUTO_SAVE_CONVERSATIONS, OFF }

enum class AmarKnowledgeUpdateAccess { VALIDATED_ONLY, ASK_BEFORE_PROMOTION, OFF }
enum class AmarSensitiveActionPolicy { ALWAYS_CONFIRM, CONFIRM_WHEN_SENSITIVE, BLOCK }

enum class AmarEvidenceVisibility { HIDDEN_UNTIL_REQUESTED, ALWAYS_VISIBLE }

/**
 * All user-adjustable behavior is centralized here so the UI does not invent policy.
 * Security-critical constraints remain fail-closed and cannot be weakened by preferences.
 */
data class AmarWorkspaceSettings(
    val multimodalAccess: AmarFeatureAccess = AmarFeatureAccess.OPEN,
    val videoAccess: AmarFeatureAccess = AmarFeatureAccess.OPEN,
    val cameraAccess: AmarCameraAccess = AmarCameraAccess.ASK_EACH_SESSION,
    val screenAccess: AmarScreenAccess = AmarScreenAccess.ASK_EACH_SESSION,
    val deviceControlAccess: AmarDeviceControlAccess = AmarDeviceControlAccess.ASK_EACH_ACTION,
    val webAccess: AmarWebAccess = AmarWebAccess.RESTRICTED,
    val memoryAccess: AmarMemoryAccess = AmarMemoryAccess.ASK_TO_SAVE,
    val knowledgeUpdateAccess: AmarKnowledgeUpdateAccess = AmarKnowledgeUpdateAccess.VALIDATED_ONLY,
    val sensitiveActionPolicy: AmarSensitiveActionPolicy = AmarSensitiveActionPolicy.ALWAYS_CONFIRM,
    val answerDetail: AmarAnswerDetail = AmarAnswerDetail.SHORT,
    val evidenceVisibility: AmarEvidenceVisibility = AmarEvidenceVisibility.HIDDEN_UNTIL_REQUESTED,
    val showReasoningProgress: Boolean = true,
    val showEngineDialogue: Boolean = true,
    val allowCopy: Boolean = true,
    val allowPaste: Boolean = true,
    val allowNotebook: Boolean = true,
    val allowResizeWorkspace: Boolean = true,
    val allowExport: AmarFeatureAccess = AmarFeatureAccess.OPEN,
    val theme: AmarThemeMode = AmarThemeMode.SYSTEM,
    val background: AmarBackgroundMode = AmarBackgroundMode.AUTOMATIC,
    val backgroundId: String = "default",
    val accentColorArgb: Long = 0xFF5B8CFF,
    val surfaceColorArgb: Long = 0xFF101522,
    val textColorArgb: Long = 0xFFFFFFFF,
    val secondaryTextColorArgb: Long = 0xFFB8C2D6,
    val fontFamily: String = "System",
    val fontSizeSp: Float = 16f,
    val fontWeight: AmarFontWeight = AmarFontWeight.NORMAL,
    val uiScale: Float = 1f
) {
    init {
        require(backgroundId.isNotBlank())
        require(fontFamily.isNotBlank())
        require(fontSizeSp in 8f..48f)
        require(uiScale in 0.75f..1.5f)
    }

    /** Preferences can never authorize what the OS/user permission boundary denies. */
    fun effectiveCamera(osPermissionGranted: Boolean): AmarCameraAccess =
        if (!osPermissionGranted) AmarCameraAccess.OFF else cameraAccess

    fun effectiveScreen(osPermissionGranted: Boolean): AmarScreenAccess =
        if (!osPermissionGranted) AmarScreenAccess.OFF else screenAccess

    fun effectiveDeviceControl(osPermissionGranted: Boolean): AmarDeviceControlAccess =
        if (!osPermissionGranted) AmarDeviceControlAccess.OFF else deviceControlAccess

    fun effectiveSensitiveActionAllowed(permissionGranted: Boolean, userConfirmed: Boolean): Boolean =
        permissionGranted && when (sensitiveActionPolicy) {
            AmarSensitiveActionPolicy.BLOCK -> false
            AmarSensitiveActionPolicy.ALWAYS_CONFIRM -> userConfirmed
            AmarSensitiveActionPolicy.CONFIRM_WHEN_SENSITIVE -> userConfirmed
        }
}

/** Atomic settings authority: one update replaces the complete user preference snapshot. */
class AmarWorkspaceSettingsStore(initial: AmarWorkspaceSettings = AmarWorkspaceSettings()) {
    private var current = initial

    fun read(): AmarWorkspaceSettings = current

    fun replace(settings: AmarWorkspaceSettings): AmarWorkspaceSettings {
        current = settings
        return current
    }
}
