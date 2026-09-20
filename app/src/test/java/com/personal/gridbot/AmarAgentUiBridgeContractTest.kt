package com.personal.gridbot

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AmarAgentUiBridgeContractTest {

    @Test
    fun ui_bridge_engine_response_path_is_wired() {
        val root = File(System.getProperty("user.dir"))
        val activity = File(root, "app/src/main/java/com/personal/gridbot/MainActivity.kt").readText()
        val html = File(root, "app/src/main/assets/amar_reference.html").readText()

        assertTrue(activity.contains("addJavascriptInterface(AmarAndroidBridge(), \"Android\")"))
        assertTrue(activity.contains("fun ask(text: String?)"))
        assertTrue(activity.contains("agentEngine.ask(\"\", \"\", request)"))
        assertTrue(activity.contains("sendAgentResult(answer, status)"))
        assertTrue(activity.contains("window.receiveAgent"))
        assertTrue(activity.contains("RESULT_DELIVERED"))

        assertTrue(html.contains("Android.ask(text)"))
        assertTrue(html.contains("window.receiveAgent=receiveAgent"))
        assertTrue(html.contains("function receiveAgent(text,status)"))
        assertTrue(html.contains("send.disabled=false"))
    }
}
