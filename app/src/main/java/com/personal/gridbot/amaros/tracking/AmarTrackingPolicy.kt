package com.personal.gridbot.amaros.tracking

/** Independent tracking hardening boundary. It validates read-only position snapshots without executing trades. */
object AmarTrackingPolicy {
    data class PositionSnapshot(
        val ticket: Long,
        val symbol: String,
        val volume: Double,
        val priceOpen: Double,
        val priceCurrent: Double,
        val profit: Double,
        val magic: Long,
        /** Broker contract size used to convert volume × price into notional exposure. */
        val contractSize: Double = 1.0,
    )

    fun validate(snapshot: PositionSnapshot): List<String> {
        val errors = mutableListOf<String>()
        if (snapshot.ticket <= 0L) errors += "TICKET_INVALID"
        if (snapshot.symbol.isBlank()) errors += "SYMBOL_REQUIRED"
        if (!snapshot.volume.isFinite() || snapshot.volume <= 0.0) errors += "VOLUME_INVALID"
        if (!snapshot.priceOpen.isFinite() || snapshot.priceOpen <= 0.0) errors += "OPEN_PRICE_INVALID"
        if (!snapshot.priceCurrent.isFinite() || snapshot.priceCurrent <= 0.0) errors += "CURRENT_PRICE_INVALID"
        if (!snapshot.profit.isFinite()) errors += "PROFIT_INVALID"
        if (snapshot.magic < 0L) errors += "MAGIC_INVALID"
        if (!snapshot.contractSize.isFinite() || snapshot.contractSize <= 0.0) errors += "CONTRACT_SIZE_INVALID"
        if (errors.isEmpty()) {
            val notional = snapshot.volume * snapshot.priceCurrent * snapshot.contractSize
            if (!notional.isFinite()) errors += "EXPOSURE_OVERFLOW"
        }
        return errors
    }

    fun validateAll(snapshots: List<PositionSnapshot>): List<String> =
        snapshots.flatMapIndexed { index, snapshot ->
            validate(snapshot).map { "POSITION_${index}_$it" }
        }

    fun totalProfit(snapshots: List<PositionSnapshot>): Double {
        require(validateAll(snapshots).isEmpty())
        val total = snapshots.sumOf { it.profit }
        require(total.isFinite()) { "PROFIT_TOTAL_OVERFLOW" }
        return total
    }

    fun exposure(snapshots: List<PositionSnapshot>): Double {
        require(validateAll(snapshots).isEmpty())
        val total = snapshots.sumOf { it.volume * it.priceCurrent * it.contractSize }
        require(total.isFinite()) { "EXPOSURE_OVERFLOW" }
        return total
    }
}
