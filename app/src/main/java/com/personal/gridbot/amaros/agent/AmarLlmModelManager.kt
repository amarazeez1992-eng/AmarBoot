package com.personal.gridbot.amaros.agent

/**
 * Lightweight model registry. It tracks metadata only; model bytes and native runtimes
 * are intentionally outside this layer.
 */
class AmarLlmModelManager(
    descriptors: List<AmarLlmModelDescriptor> = emptyList()
) {
    private val models = descriptors.associateBy { it.id }.toMutableMap()
    private var selectedId: String? = null

    fun register(model: AmarLlmModelDescriptor): Boolean {
        require(model.id.isNotBlank()) { "model id must not be blank" }
        val replaced = models.put(model.id, model) != null
        if (selectedId == null) selectedId = model.id
        return !replaced
    }

    fun remove(modelId: String): Boolean {
        val removed = models.remove(modelId) != null
        if (selectedId == modelId) selectedId = models.keys.firstOrNull()
        return removed
    }

    fun list(): List<AmarLlmModelDescriptor> = models.values.sortedBy { it.id }

    fun selected(): AmarLlmModelDescriptor? = selectedId?.let(models::get)

    fun select(modelId: String): Boolean {
        if (!models.containsKey(modelId)) return false
        selectedId = modelId
        return true
    }
}

data class AmarLlmModelDescriptor(
    val id: String,
    val displayName: String,
    val format: Format = Format.GGUF,
    val parameterCountBillion: Double? = null,
    val quantization: String? = null,
    val estimatedMemoryMb: Int? = null,
    val supportsStreaming: Boolean = true
) {
    enum class Format { GGUF, OTHER }

    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(parameterCountBillion == null || parameterCountBillion > 0.0) {
            "parameterCountBillion must be positive"
        }
        require(estimatedMemoryMb == null || estimatedMemoryMb > 0) {
            "estimatedMemoryMb must be positive"
        }
    }
}
