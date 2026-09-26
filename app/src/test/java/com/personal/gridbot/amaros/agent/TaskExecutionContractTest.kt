package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskExecutionContractTest {

    @Test
    fun task_execution_contract_is_explicit() = runBlocking {
        val task = AmarTaskUnit(
            id = "contract-task",
            kind = AmarTaskKind.CONTEXT,
            outputContract = "context-result",
            dependencies = emptyList()
        )
        val context = ContextEnvelope(
            sessionId = "contract-session",
            taskId = task.id,
            values = mapOf("input" to "contract")
        )

        val realValue = Any()
        val executor = object : TaskExecutor {
            var receivedTask: AmarTaskUnit? = null
            var receivedContext: ContextEnvelope? = null

            override suspend fun execute(
                task: AmarTaskUnit,
                context: ContextEnvelope
            ): TaskExecutionResult {
                receivedTask = task
                receivedContext = context
                return TaskExecutionResult(
                    status = TaskResultStatus.SUCCESS,
                    value = realValue,
                    provenance = listOf("contract-executor")
                )
            }
        }

        val success = executor.execute(task, context)

        assertEquals(task, executor.receivedTask)
        assertEquals(context, executor.receivedContext)
        assertNotNull(success.value)
        assertEquals(realValue, success.value)
        assertNotSame("SUCCESS must carry an execution result, not the task itself", task, success.value)
        assertEquals(TaskResultStatus.SUCCESS, success.status)

        val failed = TaskExecutionResult(
            status = TaskResultStatus.FAILED,
            value = "execution-error"
        )
        val blocked = TaskExecutionResult(
            status = TaskResultStatus.BLOCKED,
            value = "policy-stop"
        )
        assertTrue(failed.status != blocked.status)
        assertEquals(TaskResultStatus.FAILED, failed.status)
        assertEquals(TaskResultStatus.BLOCKED, blocked.status)

        fun retryAllowed(status: TaskResultStatus): Boolean =
            status == TaskResultStatus.FAILED

        assertTrue(retryAllowed(failed.status))
        assertTrue(!retryAllowed(blocked.status))

        val timeoutResult = withTimeoutOrNull(25) {
            delay(100)
            TaskExecutionResult(TaskResultStatus.SUCCESS, "late-result")
        } ?: TaskExecutionResult(
            status = TaskResultStatus.FAILED,
            value = "timeout"
        )

        assertEquals(TaskResultStatus.FAILED, timeoutResult.status)
        assertEquals("timeout", timeoutResult.value)
        assertTrue(retryAllowed(timeoutResult.status))
    }
}
