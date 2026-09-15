package com.personal.gridbot.amaros.workspace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Item10AmarProviderApiTest {
    @Test
    fun ownerCanIssueScopedCredentialAndAuthorizedCapabilityWorks() {
        val store = AmarProviderCredentialStore()
        val grant = store.issue("agent-a", setOf(AmarProviderCapability.QUERY), 100L)
        assertNotNull(grant)
        val request = AmarProviderRequest("r1", grant.credential.credentialId, grant.secret, AmarProviderCapability.QUERY, "hello", 101L)
        assertTrue(store.authorize(request))
        assertFalse(store.authorize(request.copy(capability = AmarProviderCapability.KNOWLEDGE_READ)))
    }

    @Test
    fun revocationImmediatelyBlocksCredential() {
        val store = AmarProviderCredentialStore()
        val grant = store.issue("agent-a", setOf(AmarProviderCapability.QUERY), 100L)!!
        assertTrue(store.revoke(grant.credential.credentialId))
        val request = AmarProviderRequest("r2", grant.credential.credentialId, grant.secret, AmarProviderCapability.QUERY, "hello", 101L)
        assertFalse(store.authorize(request))
        assertTrue(store.metadata(grant.credential.credentialId)!!.revoked)
    }

    @Test
    fun expiryBlocksWithoutDeletingMetadata() {
        val store = AmarProviderCredentialStore()
        val grant = store.issue("agent-a", setOf(AmarProviderCapability.QUERY), 100L, 200L)!!
        val request = AmarProviderRequest("r3", grant.credential.credentialId, grant.secret, AmarProviderCapability.QUERY, "hello", 200L)
        assertFalse(store.authorize(request))
        assertNotNull(store.metadata(grant.credential.credentialId))
    }

    @Test
    fun gatewayAuditsAndDoesNotExposeSecretInAudit() {
        val store = AmarProviderCredentialStore()
        val audit = AmarWorkspaceAuditLog()
        val gateway = AmarProviderApiGateway(store, audit, maxRequestsPerWindow = 1, windowMs = 1000L)
        val grant = store.issue("agent-a", setOf(AmarProviderCapability.QUERY), 100L)!!
        val first = gateway.handle(AmarProviderRequest("r4", grant.credential.credentialId, grant.secret, AmarProviderCapability.QUERY, "hello", 101L))
        assertTrue(first.accepted)
        val second = gateway.handle(AmarProviderRequest("r5", grant.credential.credentialId, grant.secret, AmarProviderCapability.QUERY, "hello", 102L))
        assertFalse(second.accepted)
        assertEquals(2, audit.snapshot().size)
        assertTrue(audit.snapshot().all { grant.secret !in it.reason && grant.secret !in it.actor })
    }

    @Test
    fun invalidAndWrongSecretRequestsFailClosed() {
        val store = AmarProviderCredentialStore()
        val grant = store.issue("agent-a", setOf(AmarProviderCapability.QUERY), 100L)!!
        val wrong = AmarProviderRequest("r6", grant.credential.credentialId, "wrong", AmarProviderCapability.QUERY, "hello", 101L)
        assertFalse(store.authorize(wrong))
        assertFalse(store.authorize(wrong.copy(payload = "")))
        assertFalse(store.authorize(wrong.copy(requestId = "")))
    }

    @Test
    fun disabledOrAbsentProviderCanBeRepresentedByNoCredentialWithoutCoreDependency() {
        val store = AmarProviderCredentialStore()
        assertEquals(null, store.metadata("missing"))
        val audit = AmarWorkspaceAuditLog()
        val gateway = AmarProviderApiGateway(store, audit)
        val response = gateway.handle(AmarProviderRequest("r7", "missing", "secret", AmarProviderCapability.QUERY, "hello", 101L))
        assertFalse(response.accepted)
        assertEquals("unauthorized_or_revoked", response.reason)
    }
}
