package com.personal.gridbot.amaros.core

/** Injectable time source for deterministic tests and future simulation. */
fun interface AmarClock { fun nowEpochMs(): Long }

object SystemAmarClock : AmarClock {
    override fun nowEpochMs(): Long = System.currentTimeMillis()
}
