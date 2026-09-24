package com.personal.gridbot.network

import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Field

class MetaApiClientConfigTest {
    @Test
    fun okHttpClient_has_expected_timeouts() {
        val field: Field = MetaApiClient::class.java.getDeclaredField("okHttpClient")
        field.isAccessible = true
        val client = field.get(MetaApiClient) as OkHttpClient

        assertEquals(15_000L, client.connectTimeoutMillis.toLong())
        assertEquals(30_000L, client.readTimeoutMillis.toLong())
        assertEquals(30_000L, client.writeTimeoutMillis.toLong())
    }
}
