package com.personal.gridbot.amaros.agent.correlation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarCrossSourceAgreementConsumerTest {
    private val consumer = AmarCrossSourceAgreementConsumer()

    @Test
    fun dependency_is_not_agreement() {
        val result = consumer.consume(
            correlation(
                CorrelatedGroup(setOf(fp("a"), fp("b")), CorrelationType.DEPENDENCY, emptyList())
            )
        )

        assertTrue(result.agreementGroups.isEmpty())
        assertEquals(1, result.dependencyGroups.size)
        assertEquals(CrossSourceAgreementStatus.READY, result.status)
    }

    @Test
    fun disagreement_is_not_agreement() {
        val result = consumer.consume(
            correlation(
                CorrelatedGroup(setOf(fp("a"), fp("b")), CorrelationType.DISAGREEMENT, emptyList())
            )
        )

        assertTrue(result.agreementGroups.isEmpty())
        assertEquals(1, result.disagreementGroups.size)
        assertEquals(CrossSourceAgreementStatus.READY, result.status)
    }

    @Test
    fun agreement_is_consumed_without_recalculation() {
        val group = CorrelatedGroup(setOf(fp("a"), fp("b")), CorrelationType.AGREEMENT, emptyList())
        val result = consumer.consume(correlation(group))

        assertEquals(listOf(group), result.agreementGroups)
        assertTrue(result.dependencyGroups.isEmpty())
        assertTrue(result.disagreementGroups.isEmpty())
    }

    @Test
    fun insufficient_upstream_data_fails_closed() {
        val result = consumer.consume(
            CrossSourceCorrelationResult(
                correlatedGroups = emptyList(),
                isDownstreamReady = false,
                reason = CorrelationReason.INSUFFICIENT_DATA
            )
        )

        assertEquals(
            CrossSourceAgreementStatus.INSUFFICIENT_AGREEMENT_DATA,
            result.status
        )
        assertTrue(result.agreementGroups.isEmpty())
        assertTrue(result.dependencyGroups.isEmpty())
        assertTrue(result.disagreementGroups.isEmpty())
    }

    @Test
    fun deterministic_group_order_is_preserved() {
        val first = CorrelatedGroup(setOf(fp("b"), fp("c")), CorrelationType.AGREEMENT, emptyList())
        val second = CorrelatedGroup(setOf(fp("a"), fp("d")), CorrelationType.AGREEMENT, emptyList())

        val result = consumer.consume(correlation(first, second))

        assertEquals(listOf(second, first), result.agreementGroups)
    }

    private fun correlation(vararg groups: CorrelatedGroup) =
        CrossSourceCorrelationResult(
            correlatedGroups = groups.toList(),
            isDownstreamReady = true,
            reason = CorrelationReason.VALID_CORRELATION
        )

    private fun fp(seed: String): String = seed.repeat(64).take(64)
}
