package com.personal.gridbot.amaros.bots

data class AmarBot1StrategyProfile(
    val strategyId: String,
    val name: String,
    val config: AmarBot1RuntimeConfig,
    val riskProfile: String = "STANDARD",
    val rebuildRules: String = "DEFAULT",
    val entryRules: String = "BOT1_DEFAULT",
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(strategyId.isNotBlank())
        require(name.isNotBlank())
        require(riskProfile.isNotBlank())
        require(rebuildRules.isNotBlank())
        require(entryRules.isNotBlank())
    }
}

object AmarBot1StrategyCatalog {
    const val SLOT_COUNT = 10

    fun defaults(): List<AmarBot1StrategyProfile> = (1..SLOT_COUNT).map { slot ->
        AmarBot1StrategyProfile(
            strategyId = "STRATEGY_%02d".format(slot),
            name = "استراتيجية رقم $slot",
            config = AmarBot1RuntimeConfig(),
            metadata = mapOf("slot" to slot.toString())
        )
    }
}
