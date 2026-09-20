package com.personal.gridbot

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebSettings
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.personal.gridbot.amaros.ai.AmarAiActionEngine
import com.personal.gridbot.amaros.ai.AmarAiAgentEngine
import com.personal.gridbot.amaros.ai.AmarAiAppCommandBus
import com.personal.gridbot.amaros.ai.AmarAiSelfImprovementScheduler
import com.personal.gridbot.amaros.navigation.AmarRoom
import com.personal.gridbot.amaros.navigation.AmarRoomHostScreen
import com.personal.gridbot.amaros.runtime.AmarBotCommandEngine
import com.personal.gridbot.amaros.security.AmarProtectionCenter
import com.personal.gridbot.amaros.visual.AmarGlobalVisualOverlay
import com.personal.gridbot.amaros.visual.AmarVisualEffectsPreference
import com.personal.gridbot.amaros.visual.AmarGlobalVisualStateStore
import com.personal.gridbot.ui.theme.AmarDay
import com.personal.gridbot.ui.theme.AmarPlatinum
import com.personal.gridbot.ui.theme.AmarTheme
import com.personal.gridbot.ui.theme.AmarThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private var home: WebView? = null
    private var roomHost: ComposeView? = null
    private var visualOverlay: ComposeView? = null
    private var showingRoom = false
    private var currentRoom: AmarRoom? = null
    private var themeMode by mutableStateOf(AmarThemeMode.DARK)
    private var startupFinished = false
    private var backgroundSystemsStarted = false
    private var webViewRecoveryAttempted = false
    private var homePageReady = false
    private var pendingAgentResult: Pair<String, String>? = null
    private val agentEngine = AmarAiAgentEngine()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installProtectionHandler(); super.onCreate(savedInstanceState)
        root = FrameLayout(this); setContentView(root)
        runCatching {
            themeMode = AmarThemeMode.DARK
            AmarGlobalVisualStateStore.setEnabled(runCatching { AmarVisualEffectsPreference.load(this) }.getOrDefault(true))
            enterImmersiveReferenceMode()
        }.onFailure { error -> showStartupError("تهيئة التطبيق", error) }
        root.post { initializeHomeSafely() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeHomeSafely() {
        if (startupFinished || isFinishing || isDestroyed) return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && WebView.getCurrentWebViewPackage() == null) throw IllegalStateException("لا يوجد مزود Android WebView صالح على الجهاز")
            val web = buildHomeWebView(); home = web; root.addView(web, 0, FrameLayout.LayoutParams(-1, -1)); startupFinished = true
        }.onFailure { error -> Log.e("AMAR_STARTUP", "Home WebView initialization failed", error); showStartupError("تشغيل واجهة التطبيق", error) }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildHomeWebView(): WebView = WebView(this).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                homePageReady = true
                deliverPendingAgentResult()
                runCatching { ensureRoomHost(); ensureVisualOverlay(); startBackgroundSystemsOnce() }.onFailure { error -> Log.e("AMAR_STARTUP", "Home page initialization failed", error); showStartupError("تهيئة واجهة AMAR AI", error) } }
            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                homePageReady = false
                Log.e("AMAR_STARTUP", "WebView renderer terminated; crashed=${detail?.didCrash()}")
                runOnUiThread { recoverWebViewAfterRendererGone(view) }
                return true
            }
        }
        settings.javaScriptEnabled = true; settings.domStorageEnabled = true; settings.cacheMode = WebSettings.LOAD_DEFAULT; settings.allowFileAccess = true; settings.allowContentAccess = false; settings.builtInZoomControls = false; settings.displayZoomControls = false
        addJavascriptInterface(AmarAndroidBridge(), "Android")
        loadUrl("file:///android_asset/amar_reference.html")
    }

    private inner class AmarAndroidBridge {
        @JavascriptInterface
        fun openRoom(roomName: String?) {
            val room = runCatching { AmarRoom.valueOf(roomName?.trim().orEmpty()) }.getOrNull() ?: return
            runOnUiThread { showRoom(room) }
        }

        @JavascriptInterface
        fun ask(text: String?) {
            val request = text?.trim().orEmpty()
            if (request.isEmpty()) return
            Log.i("AMAR_AGENT_BRIDGE", "ASK_RECEIVED length=${request.length}")
            sendAgentStatus("Agent: يبحث ويتحقق… قد يستغرق وقتًا إضافيًا لضمان الدقة")
            askAgent(request) { answer, status -> sendAgentResult(answer, status) }
        }
    }

    private fun askAgent(request: String, onResult: (String, String) -> Unit) {
        lifecycleScope.launch {
            val startedAt = System.currentTimeMillis()
            Log.i("AMAR_AGENT", "REQUEST_START")
            val local = runCatching { AmarAiActionEngine.route(request) }.getOrNull()
            if (local?.handled == true) {
                val elapsedMs = System.currentTimeMillis() - startedAt
                Log.i("AMAR_AGENT", "LOCAL_ACTION_HANDLED elapsedMs=" + elapsedMs)
                onResult(
                    local.response,
                    "Agent: جاهز | زمن المعالجة والتحقق: " + (elapsedMs / 1000) + "ث | المصادر المكتشفة: 0 | المصادر المعتمدة: 0"
                )
                return@launch
            }
            val result = runCatching {
                withTimeout(120_000L) {
                    withContext(Dispatchers.Default) { agentEngine.ask("", "", request) { progress ->
                        val elapsed = progress.elapsedMs / 1000
                        sendAgentStatus("Agent: ${progress.message} | الزمن: ${elapsed}ث | المصادر المكتشفة: ${progress.sourcesSearched} | المصادر المعتمدة: ${progress.sourcesAccepted}")
                    } }
                }
            }
            result.onSuccess {
                Log.i("AMAR_AGENT", "REQUEST_SUCCESS elapsedMs=${System.currentTimeMillis() - startedAt}")
                val answer = it.answer.ifBlank { "لم يُنتج الوكيل إجابة." }
                onResult(answer, "Agent: جاهز | زمن المعالجة والتحقق: ${it.elapsedMs / 1000}ث | المصادر المكتشفة: ${it.sourcesSearched} | المصادر المعتمدة: ${it.sourcesAccepted}")
            }.onFailure { error ->
                Log.e("AMAR_AGENT", "REQUEST_FAILED elapsedMs=${System.currentTimeMillis() - startedAt}", error)
                val message = if (error is kotlinx.coroutines.TimeoutCancellationException) {
                    "انتهت مهلة التحقق بعد 120 ثانية. لم يتم اختلاق إجابة أو تجاوز طبقات التحقق."
                } else {
                    "تعذر تمرير الطلب إلى AMAR AI Agent: " + (error.message ?: error.javaClass.simpleName)
                }
                onResult(message, "Agent: خطأ")
            }
        }
    }
    private fun sendAgentStatus(status: String) {
        val script = "window.setAgentStatus && window.setAgentStatus(${org.json.JSONObject.quote(status)})"
        runOnUiThread {
            home?.post { home?.evaluateJavascript(script, null) }
        }
    }

    private fun sendAgentResult(answer: String, status: String) {
        Log.i("AMAR_AGENT_BRIDGE", "RESULT_READY status=$status answerLength=${answer.length}")
        pendingAgentResult = answer to status
        deliverPendingAgentResult()
    }

    private var agentDeliveryAttempts = 0

    private fun deliverPendingAgentResult() {
        val pending = pendingAgentResult ?: return
        runOnUiThread {
            val web = home ?: return@runOnUiThread
            if (!homePageReady) return@runOnUiThread
            web.post {
                val quotedAnswer = org.json.JSONObject.quote(pending.first)
                val quotedStatus = org.json.JSONObject.quote(pending.second)
                val deliver = """
                    (function() {
                        try {
                            if (typeof window.receiveAgent !== 'function') return 'NOT_READY';
                            window.receiveAgent($quotedAnswer, $quotedStatus);
                            return 'DELIVERED';
                        } catch (e) {
                            return 'ERROR:' + String(e);
                        }
                    })()
                """.trimIndent()
                web.evaluateJavascript(deliver) { result ->
                    when {
                        result == "\"DELIVERED\"" -> {
                            Log.i("AMAR_AGENT_BRIDGE", "RESULT_DELIVERED")
                            pendingAgentResult = null
                            agentDeliveryAttempts = 0
                        }
                        agentDeliveryAttempts < 20 -> {
                            agentDeliveryAttempts += 1
                            web.postDelayed({ deliverPendingAgentResult() }, 250L)
                        }
                        else -> {
                            Log.e("AMAR_AGENT_BRIDGE", "receiveAgent delivery failed after retries: $result")
                            agentDeliveryAttempts = 0
                        }
                    }
                }
            }
        }
    }
    private fun recoverWebViewAfterRendererGone(deadView: WebView?) {
        if (isFinishing || isDestroyed) return
        if (webViewRecoveryAttempted) { showStartupError("محرك WebView", IllegalStateException("تم إنهاء محرك WebView أكثر من مرة")); return }
        webViewRecoveryAttempted = true
        homePageReady = false
        runCatching { deadView?.let { root.removeView(it); it.stopLoading(); it.removeAllViews(); it.destroy() }; home = null; val replacement = buildHomeWebView(); home = replacement; root.addView(replacement, 0, FrameLayout.LayoutParams(-1, -1)) }.onFailure { error -> Log.e("AMAR_STARTUP", "WebView recovery failed", error); showStartupError("استرداد WebView", error) }
    }

    private fun ensureRoomHost() { if (roomHost != null) return; val host = ComposeView(this).apply { visibility = android.view.View.GONE }; roomHost = host; root.addView(host, FrameLayout.LayoutParams(-1, -1)) }
    private fun ensureVisualOverlay() { if (visualOverlay != null) return; val overlay = ComposeView(this).apply { setContent { AmarGlobalVisualOverlay(this@MainActivity) } }; visualOverlay = overlay; root.addView(overlay, FrameLayout.LayoutParams(-1, -1)) }

    private fun startBackgroundSystemsOnce() {
        if (backgroundSystemsStarted) return; backgroundSystemsStarted = true
        lifecycleScope.launch { runCatching { AmarAiSelfImprovementScheduler.start(this@MainActivity) } }
        startAiCommandBridge()
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) { override fun handleOnBackPressed() { if (showingRoom) showHome() else finish() } })
    }

    private fun startAiCommandBridge() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                AmarAiAppCommandBus.commands.collect { command ->
                    when (command) {
                        is AmarAiAppCommandBus.Command.OpenRoom -> showRoom(command.room)
                        is AmarAiAppCommandBus.Command.SetVisualEffects -> setVisualEffectsEnabled(command.enabled)
                        is AmarAiAppCommandBus.Command.SetTheme -> onThemeModeChanged(command.mode)
                        is AmarAiAppCommandBus.Command.SetBackground -> applyBackground(command.background)
                        is AmarAiAppCommandBus.Command.SetAccentColor -> applyAccentColor(command.color)
                        AmarAiAppCommandBus.Command.RequestUpdate -> requestApplicationUpdate()
                        is AmarAiAppCommandBus.Command.QueueBotCommand -> runCatching { AmarBotCommandEngine(this@MainActivity).queue(command.botNumber, command.command) }
                    }
                }
            }
        }
    }

    private fun applyBackground(background: String) { home?.evaluateJavascript("window.amarAiSetBackground && window.amarAiSetBackground(${org.json.JSONObject.quote(background)})", null) }
    private fun applyAccentColor(color: String) { home?.evaluateJavascript("window.amarAiSetAccentColor && window.amarAiSetAccentColor(${org.json.JSONObject.quote(color)})", null) }
    private fun requestApplicationUpdate() { home?.evaluateJavascript("window.amarAiRequestUpdate && window.amarAiRequestUpdate()", null) }

    private fun installProtectionHandler() { val previous = Thread.getDefaultUncaughtExceptionHandler(); Thread.setDefaultUncaughtExceptionHandler { thread, error -> runCatching { AmarProtectionCenter.recordFailure(this, currentRoom?.titleAr ?: "بدء التشغيل", error) }; previous?.uncaughtException(thread, error) } }
    private fun enterImmersiveReferenceMode() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                    android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
    private fun effectiveDark(): Boolean = when (themeMode) { AmarThemeMode.DARK -> true; AmarThemeMode.LIGHT -> false; AmarThemeMode.AUTO -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES }
    private fun palette() = if (effectiveDark()) AmarPlatinum else AmarDay
    private fun renderCurrentRoom() { val room = currentRoom ?: return; roomHost?.setContent { AmarTheme(palette(), themeMode) { AmarRoomHostScreen(room, ::showHome, themeMode, ::onThemeModeChanged, ::askAgentForCompose) } } }
    private fun askAgentForCompose(request: String, onAnswer: (String) -> Unit) {
        askAgent(request) { answer, _ -> runOnUiThread { onAnswer(answer) } }
    }
    private fun showRoom(room: AmarRoom) { ensureRoomHost(); ensureVisualOverlay(); currentRoom = room; showingRoom = true; home?.visibility = android.view.View.GONE; roomHost?.visibility = android.view.View.VISIBLE; renderCurrentRoom() }
    private fun onThemeModeChanged(mode: AmarThemeMode) { themeMode = mode; renderCurrentRoom() }
    private fun showHome() { showingRoom = false; currentRoom = null; roomHost?.visibility = android.view.View.GONE; home?.visibility = android.view.View.VISIBLE }
    private fun setVisualEffectsEnabled(enabled: Boolean) { AmarVisualEffectsPreference.save(this, enabled); AmarGlobalVisualStateStore.setEnabled(enabled) }
    private fun showStartupError(stage: String, error: Throwable) { runCatching { AmarProtectionCenter.recordFailure(this, stage, error); val message = buildString { append(error.javaClass.simpleName); if (!error.message.isNullOrBlank()) append("\n").append(error.message) }.take(260); val recovery = ComposeView(this).apply { setContent { Surface(Modifier.fillMaxSize(), color = Color(0xFF07121B)) { Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text("AMAR AI", color = Color(0xFF19E6FF), fontSize = 30.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(10.dp)); Text("تعذر تشغيل التطبيق", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Text(stage, color = Color(0xFFFFD36A), textAlign = TextAlign.Center); Spacer(Modifier.height(8.dp)); Text(message, color = Color(0xFF9DB5BF), fontSize = 11.sp, textAlign = TextAlign.Center) } } } }; root.removeAllViews(); root.addView(recovery, FrameLayout.LayoutParams(-1, -1)) }.onFailure { Log.e("AMAR_STARTUP", "Failed to render startup error", error) } }
    override fun onDestroy() { home?.let { web -> runCatching { root.removeView(web); web.stopLoading(); web.removeAllViews(); web.destroy() } }; home = null; super.onDestroy() }
}
