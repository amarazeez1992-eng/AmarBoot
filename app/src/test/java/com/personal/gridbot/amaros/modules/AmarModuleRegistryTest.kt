package com.personal.gridbot.amaros.modules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AmarModuleRegistryTest {
    private interface TestModule
    private class FirstModule : TestModule
    private class SecondModule : TestModule

    @Test
    fun register_and_resolve_returns_registered_instance() {
        val instance = FirstModule()
        AmarModuleRegistry.register(TestModule::class, instance)
        assertEquals(instance, AmarModuleRegistry.resolve(TestModule::class))
    }

    @Test
    fun resolve_unregistered_module_throws_exception() {
        assertThrows(IllegalStateException::class.java) {
            AmarModuleRegistry.resolve(TestModule::class)
        }
    }

    @Test
    fun registering_again_replaces_previous_instance() {
        val first = FirstModule()
        val second = SecondModule()
        AmarModuleRegistry.register(TestModule::class, first)
        AmarModuleRegistry.register(TestModule::class, second)
        assertEquals(second, AmarModuleRegistry.resolve(TestModule::class))
    }
}
