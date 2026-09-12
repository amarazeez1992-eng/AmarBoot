package com.personal.gridbot.amaros.agent

/** Metadata only: model weights are never committed to the public repository. */
class AmarModelRegistry {
    private val models = linkedMapOf<String, AmarModelSpec>()

    fun register(model: AmarModelSpec) { models[model.id] = model }
    fun get(id: String): AmarModelSpec? = models[id]
    fun all(): List<AmarModelSpec> = models.values.toList()
}

data class AmarModelSpec(
    val id: String,
    val format: String = "GGUF",
    val parameterBillions: Double,
    val quantization: String,
    val estimatedRamMb: Int,
    val contextTokens: Int,
    val license: String,
    val sourceUri: String
) {
    init {
        require(parameterBillions > 0.0)
        require(estimatedRamMb > 0)
        require(contextTokens > 0)
    }
}
