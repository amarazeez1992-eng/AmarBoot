package com.personal.gridbot.amaros.ai.integration

/** AI-side contracts for a future MT5 integration. No broker I/O or execution is implemented. */
enum class AmarMt5AccessMode { READ_ONLY, PAPER, LIVE }

data class AmarMt5Snapshot(
    val accountId: String,
    val symbol: String,
    val balance: Double,
    val equity: Double,
    val positions: Int,
) {
    init {
        require(accountId.isNotBlank())
        require(symbol.isNotBlank())
        require(balance.isFinite() && equity.isFinite())
        require(positions >= 0)
    }
}

data class AmarMt5Command(
    val idempotencyKey: String,
    val action: String,
    val approved: Boolean,
) {
    init {
        require(idempotencyKey.isNotBlank())
        require(action.isNotBlank())
    }
}

data class AmarMt5Ack(
    val idempotencyKey: String,
    val accepted: Boolean,
    val readBackRequired: Boolean = true,
)

object AmarMt5IntegrationGateway {
    fun allowSnapshot(mode: AmarMt5AccessMode): Boolean = mode == AmarMt5AccessMode.READ_ONLY

    fun allowCommand(mode: AmarMt5AccessMode, command: AmarMt5Command): Boolean =
        mode == AmarMt5AccessMode.PAPER && command.approved
}

class AmarMt5IdempotencyLedger {
    private val seen = mutableSetOf<String>()

    fun acceptOnce(key: String): Boolean =
        key.isNotBlank() && seen.add(key)
}

class AmarMt5KillSwitch(initiallyEnabled: Boolean = true) {
    var enabled: Boolean = initiallyEnabled
        private set

    fun activate() { enabled = true }
    fun deactivate() { enabled = false }
}
