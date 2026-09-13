package com.personal.gridbot.amaros.position

/**
 * Multi-position command planning boundary. It validates and previews commands;
 * it never modifies broker positions.
 */
object AmarPositionCommandCenter {
    enum class Action { SET_STOP_LOSS, SET_TAKE_PROFIT, PARTIAL_CLOSE, CLOSE, BREAK_EVEN, TRAILING }

    data class Position(
        val ticket: Long,
        val symbol: String,
        val volume: Double,
        val openPrice: Double,
    )

    data class Command(
        val action: Action,
        val tickets: List<Long>,
        val price: Double? = null,
        val trailingDistance: Double? = null,
        val closeVolume: Double? = null,
    )

    data class Preview(
        val accepted: Boolean,
        val errors: List<String>,
        val affectedTickets: List<Long>,
    )

    fun validatePositions(positions: List<Position>): List<String> = buildList {
        val tickets = mutableSetOf<Long>()
        positions.forEachIndexed { index, position ->
            if (position.ticket <= 0L) add("POSITION_${index}_TICKET_INVALID")
            else if (!tickets.add(position.ticket)) add("POSITION_${index}_TICKET_DUPLICATE")
            if (position.symbol.isBlank()) add("POSITION_${index}_SYMBOL_REQUIRED")
            if (!position.volume.isFinite() || position.volume <= 0.0) add("POSITION_${index}_VOLUME_INVALID")
            if (!position.openPrice.isFinite() || position.openPrice <= 0.0) add("POSITION_${index}_OPEN_PRICE_INVALID")
        }
    }

    fun preview(command: Command, positions: List<Position>): Preview {
        val errors = mutableListOf<String>()
        errors += validatePositions(positions)
        if (command.tickets.isEmpty()) errors += "TICKETS_REQUIRED"
        if (command.tickets.distinct().size != command.tickets.size) errors += "TICKETS_DUPLICATE"
        if (command.tickets.any { it <= 0L }) errors += "TICKET_INVALID"
        val selected = positions.filter { it.ticket in command.tickets }
        if (selected.size != command.tickets.distinct().size) errors += "POSITION_NOT_FOUND"

        when (command.action) {
            Action.SET_STOP_LOSS, Action.SET_TAKE_PROFIT, Action.BREAK_EVEN -> {
                if (command.price == null) errors += "PRICE_REQUIRED"
                else if (!command.price.isFinite() || command.price <= 0.0) errors += "PRICE_INVALID"
                if (command.trailingDistance != null) errors += "TRAILING_PARAMETER_NOT_ALLOWED"
            }
            Action.TRAILING -> {
                if (command.trailingDistance == null) errors += "TRAILING_DISTANCE_REQUIRED"
                else if (!command.trailingDistance.isFinite() || command.trailingDistance <= 0.0) errors += "TRAILING_DISTANCE_INVALID"
                if (command.price != null) errors += "PRICE_NOT_ALLOWED_FOR_TRAILING"
            }
            Action.PARTIAL_CLOSE -> {
                if (command.closeVolume == null || !command.closeVolume.isFinite() || command.closeVolume <= 0.0) {
                    errors += "CLOSE_VOLUME_REQUIRED"
                } else if (selected.any { command.closeVolume >= it.volume }) {
                    errors += "PARTIAL_VOLUME_MUST_BE_LESS_THAN_POSITION"
                }
                if (command.price != null || command.trailingDistance != null) errors += "CLOSE_PARAMETERS_NOT_ALLOWED"
            }
            Action.CLOSE -> if (command.price != null || command.trailingDistance != null || command.closeVolume != null) {
                errors += "CLOSE_PARAMETERS_NOT_ALLOWED"
            }
        }
        return Preview(errors.isEmpty(), errors, selected.map { it.ticket })
    }
}
