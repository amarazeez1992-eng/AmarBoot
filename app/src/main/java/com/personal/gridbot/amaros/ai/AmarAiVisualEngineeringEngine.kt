package com.personal.gridbot.amaros.ai

/**
 * Visual engineering contract for AMAR AI.
 *
 * This layer classifies visual requests and creates a deterministic execution plan.
 * Actual image generation/editing is delegated to an explicitly connected visual tool;
 * no fake image-generation success is reported when such a tool is unavailable.
 */
object AmarAiVisualEngineeringEngine {
    enum class Mode {
        IDEA_TO_DESIGN,
        CODE_TO_VISUAL,
        IMAGE_TO_CODE,
        IMAGE_EDIT,
        HIGH_RES_IMAGE_GENERATION,
        UI_REDESIGN
    }

    data class VisualPlan(
        val mode: Mode,
        val request: String,
        val outputs: List<String>,
        val workflow: List<String>,
        val validation: List<String>,
        val authority: AmarAiToolRegistry.Authority = AmarAiToolRegistry.Authority.DRAFT_ONLY
    )

    fun plan(request: String): VisualPlan {
        val q = request.lowercase()
        val mode = when {
            containsAny(q, "صورة", "image", "screenshot", "لقطة شاشة") &&
                containsAny(q, "كود", "code", "html", "css", "compose", "xml") -> Mode.IMAGE_TO_CODE
            containsAny(q, "الكود", "code", "html", "css", "compose", "xml") &&
                containsAny(q, "صورة", "تصميم", "معاينة", "visual", "preview", "render") -> Mode.CODE_TO_VISUAL
            containsAny(q, "عدّل الصورة", "عدل الصورة", "edit image", "تحرير الصورة", "تعديل الصورة") -> Mode.IMAGE_EDIT
            containsAny(q, "دقة عالية", "عالية الدقة", "4k", "8k", "high resolution", "high-res") -> Mode.HIGH_RES_IMAGE_GENERATION
            containsAny(q, "إعادة تصميم", "صمم الواجهة", "صمّم الواجهة", "redesign", "design ui", "ui design") -> Mode.UI_REDESIGN
            else -> Mode.IDEA_TO_DESIGN
        }

        return VisualPlan(
            mode = mode,
            request = request,
            outputs = when (mode) {
                Mode.IDEA_TO_DESIGN -> listOf("design_spec", "component_tree", "responsive_layout", "implementation_plan")
                Mode.CODE_TO_VISUAL -> listOf("render_plan", "ui_screenshot_spec", "design_tokens", "component_map")
                Mode.IMAGE_TO_CODE -> listOf("component_tree", "layout_code", "style_code", "asset_map", "responsive_rules")
                Mode.IMAGE_EDIT -> listOf("edit_plan", "preservation_constraints", "edited_image_request")
                Mode.HIGH_RES_IMAGE_GENERATION -> listOf("high_resolution_image_request", "negative_constraints", "export_spec")
                Mode.UI_REDESIGN -> listOf("current_ui_audit", "redesign_spec", "component_plan", "migration_plan")
            },
            workflow = listOf(
                "analyze_request",
                "inspect_existing_code_or_image_when_available",
                "identify_platform_and_constraints",
                "choose_appropriate_ui_libraries_and_assets",
                "produce_design_or_visual_tool plan",
                "generate code/specification or invoke connected visual tool",
                "validate accessibility_responsiveness_consistency",
                "report verified results separately from proposals"
            ),
            validation = listOf(
                "no fabricated rendering result",
                "preserve user-provided visual intent",
                "check responsive behavior",
                "check asset and font licenses",
                "check dependency compatibility",
                "compile/test generated code when toolchains are available"
            )
        )
    }

    fun toArabicReport(plan: VisualPlan): String = buildString {
        append("هندسة بصرية AMAR AI\n")
        append("النمط: ${plan.mode}\n")
        append("المخرجات: ${plan.outputs.joinToString("، ")}\n")
        append("المسار: ${plan.workflow.joinToString(" → ")}\n")
        append("التحقق: ${plan.validation.joinToString("؛ ")}\n")
        append("الصلاحية: ${plan.authority}\n")
        append("تنبيه: إنشاء/تعديل الصورة فعلياً يحتاج أداة صور متصلة ومصرحاً بها؛ لا يتم ادعاء نجاح غير متحقق منه.")
    }

    private fun containsAny(value: String, vararg terms: String): Boolean = terms.any(value::contains)
}
