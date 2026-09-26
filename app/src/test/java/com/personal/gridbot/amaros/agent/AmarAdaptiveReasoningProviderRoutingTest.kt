package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmarAdaptiveReasoningProviderRoutingTest {
 private class P(override val id:String, val fail:Boolean=false, val text:String="generated"):AmarModelProvider {
  override suspend fun load(modelPath:String)=AmarModelInfo(id,"test",8192,256,true)
  override suspend fun generate(request:AmarGenerationRequest):AmarGenerationResult { if(fail) error("failure"); return AmarGenerationResult(text) }
  override suspend fun unload()=Unit
 }
 private fun p(id:String,q:AmarModelQuality=AmarModelQuality.HIGH,ram:Int=256,cost:AmarModelCost=AmarModelCost.LOW,provider:AmarModelProvider=P(id))=AmarModelProviderProfile(provider,setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS),8192,ram,q,cost)
 @Test fun resource_rejects(){val a=AmarAdaptiveReasoningProvider(AmarModelProviderCatalog(listOf(p("x",ram=4096))));assertNull(a.selectProvider(AmarModelTask.MULTI_FACTOR_ANALYSIS,AmarModelComplexity.HIGH,4096,1024,AmarModelQuality.STANDARD).selectedProvider)}
 @Test fun quality_rejects_unknown(){val a=AmarAdaptiveReasoningProvider(AmarModelProviderCatalog(listOf(p("x",q=AmarModelQuality.UNKNOWN))));assertNull(a.selectProvider(AmarModelTask.MULTI_FACTOR_ANALYSIS,AmarModelComplexity.HIGH,4096,4096,AmarModelQuality.STANDARD).selectedProvider)}
 @Test fun cost_rejects_unknown_when_constrained(){val a=AmarAdaptiveReasoningProvider(AmarModelProviderCatalog(listOf(p("x",cost=AmarModelCost.UNKNOWN))));assertNull(a.selectProvider(AmarModelTask.MULTI_FACTOR_ANALYSIS,AmarModelComplexity.HIGH,4096,4096,AmarModelQuality.STANDARD,AmarModelCost.MEDIUM).selectedProvider)}
 @Test fun fallback_tries_secondary()=runBlocking{val audit=AmarModelRoutingAudit();val a=AmarAdaptiveReasoningProvider(AmarModelProviderCatalog(listOf(p("a",provider=P("a",true)),p("b",provider=P("b",text="secondary")))),audit);assertEquals("secondary",a.generate(AmarAgentContext("compare",emptyList(),false,false),AmarModelTask.MULTI_FACTOR_ANALYSIS,AmarModelComplexity.HIGH,4096,4096,AmarModelQuality.STANDARD).answer);assertEquals("FAILED",audit.records().first{it.selectedProvider=="a"}.decisionState)}
}
