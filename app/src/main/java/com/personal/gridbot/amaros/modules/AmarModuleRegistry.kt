package com.personal.gridbot.amaros.modules

import kotlin.reflect.KClass

object AmarModuleRegistry {
    private val registry = mutableMapOf<KClass<*>, Any>()

    fun <T : Any> register(type: KClass<T>, instance: T) {
        registry[type] = instance
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolve(type: KClass<T>): T =
        registry[type] as? T
            ?: error("Module not registered: ${type.qualifiedName}")
}
