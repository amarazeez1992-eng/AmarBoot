package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AmarReasoningRouterTest {
    private class Local : AmarReasoningProvider {
        var calls=0
        override suspend fun respond(context: AmarAgentContext): AmarAgentResponse { calls++; return AmarAgentResponse("local") }
    }
    private class Model(override val id:String): AmarModelProvider {
        var calls=0
        override suspend fun load(modelPath:String)=AmarModelInfo(id,"test",8192,256,true)
        override suspend fun generate(request:AmarGenerationRequest):AmarGenerationResult { calls++; return AmarGenerationResult("adaptive") }
        override suspend fun unload()=Unit
    }
    private fun router(local:Local, model:Model?=null):AmarReasoningRouter {
        val audit=AmarModelRoutingAudit()
        val catalog=if(model==null) AmarModelProviderCatalog() else AmarModelProviderCatalog(listOf(AmarModelProviderProfile(model,setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS),8192,256,AmarModelQuality.HIGH,AmarModelCost.LOW)))
        return AmarReasoningRouter(local,AmarAdaptiveReasoningProvider(catalog,audit),AmarModelTaskClassifier(),AmarModelComplexityEstimator(),audit)
    }
    @Test fun low_routes_to_injected_local()=runBlocking { val l=Local(); assertEquals("local",router(l).respond(AmarAgentContext("hello",emptyList(),false,false)).answer); assertEquals(1,l.calls) }
    @Test fun high_without_provider_fails_closed()=runBlocking { assertEquals(AmarAgentResponse.Status.ERROR,router(Local()).respond(AmarAgentContext("compare multiple factors in detail",emptyList(),false,false)).status) }
    @Test fun high_routes_to_adaptive_provider()=runBlocking { val p=Model("adaptive"); assertEquals("adaptive",router(Local(),p).respond(AmarAgentContext("compare multiple factors in detail",emptyList(),false,false)).answer); assertEquals(1,p.calls) }
    @Test fun unknown_task_fails_closed()=runBlocking { assertEquals(AmarAgentResponse.Status.ERROR,router(Local()).respond(AmarAgentContext("",emptyList(),false,false)).status) }
}
