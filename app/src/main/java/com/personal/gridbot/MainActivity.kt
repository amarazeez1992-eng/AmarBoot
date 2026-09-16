package com.personal.gridbot

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.personal.gridbot.amaros.ai.AmarAiAppCommandBus
import com.personal.gridbot.amaros.ai.AmarAiEngineBinding
import com.personal.gridbot.amaros.ai.AmarAiSelfImprovementScheduler
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.design.AmarAiOrbMarketMotionController
import com.personal.gridbot.amaros.design.AmarHomeLayoutController
import com.personal.gridbot.amaros.design.AmarSharedUiContract
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingDiscoveryScheduler
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private var home: WebView? = null
    private var roomHost: ComposeView? = null
    private var visualOverlay: ComposeView? = null
    private var showingRoom = false
    private var currentRoom: AmarRoom? = null
    private var themeMode by mutableStateOf(AmarThemeMode.DARK)
    private var homeLayout by mutableStateOf(AmarHomeLayoutController.DEFAULT_LAYOUT)
    private var webViewRecoveryAttempted = false
    private var startupFinished = false
    private var backgroundSystemsStarted = false
    private val prefs by lazy { getSharedPreferences(AmarSharedUiContract.PREFS, MODE_PRIVATE) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installProtectionHandler()
        super.onCreate(savedInstanceState)
        root = FrameLayout(this)
        setContentView(root)
        runCatching {
            themeMode = AmarThemeMode.valueOf(prefs.getString(AmarSharedUiContract.PREF_THEME_MODE, AmarThemeMode.DARK.name) ?: AmarThemeMode.DARK.name)
            homeLayout = prefs.getInt(AmarSharedUiContract.PREF_HOME_LAYOUT, AmarHomeLayoutController.DEFAULT_LAYOUT).coerceIn(AmarHomeLayoutController.DEFAULT_LAYOUT, AmarHomeLayoutController.LAYOUT_COUNT)
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
            val web = buildHomeWebView()
            home = web
            root.addView(web, 0, FrameLayout.LayoutParams(-1, -1))
            startupFinished = true
        }.onFailure { error ->
            Log.e("AMAR_STARTUP", "Home WebView initialization failed", error)
            showStartupError("تشغيل واجهة التطبيق", error)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildHomeWebView(): WebView = WebView(this).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                runCatching {
                    enhanceHome()
                    applyHomeTheme()
                    ensureRoomHost()
                    ensureVisualOverlay()
                    startBackgroundSystemsOnce()
                }.onFailure { error ->
                    Log.e("AMAR_STARTUP", "Home page initialization failed", error)
                    showStartupError("تهيئة واجهة عمار", error)
                }
            }
            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                Log.e("AMAR_STARTUP", "WebView renderer terminated; crashed=${detail?.didCrash()}")
                runOnUiThread { recoverWebViewAfterRendererGone(view) }
                return true
            }
        }
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.allowFileAccess = true
        settings.allowContentAccess = false
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        addJavascriptInterface(HomeBridge(), "Android")
        loadUrl("file:///android_asset/amar_ai_workspace.html")
    }

    private fun recoverWebViewAfterRendererGone(deadView: WebView?) {
        if (isFinishing || isDestroyed) return
        if (webViewRecoveryAttempted) { showStartupError("محرك WebView", IllegalStateException("تم إنهاء محرك WebView أكثر من مرة")); return }
        webViewRecoveryAttempted = true
        runCatching {
            deadView?.let { root.removeView(it); it.stopLoading(); it.removeAllViews(); it.destroy() }
            home = null
            val replacement = buildHomeWebView()
            home = replacement
            root.addView(replacement, 0, FrameLayout.LayoutParams(-1, -1))
        }.onFailure { error ->
            Log.e("AMAR_STARTUP", "WebView recovery failed", error)
            showStartupError("استرداد WebView", error)
        }
    }

    private fun ensureRoomHost() {
        if (roomHost != null) return
        val host = ComposeView(this).apply { visibility = android.view.View.GONE }
        roomHost = host
        root.addView(host, FrameLayout.LayoutParams(-1, -1))
    }

    private fun ensureVisualOverlay() {
        if (visualOverlay != null) return
        val overlay = ComposeView(this).apply { setContent { AmarGlobalVisualOverlay(this@MainActivity) } }
        visualOverlay = overlay
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))
    }

    private fun startBackgroundSystemsOnce() {
        if (backgroundSystemsStarted) return
        backgroundSystemsStarted = true
        lifecycleScope.launch {
            runCatching { AmarTradingDiscoveryScheduler.start(this@MainActivity) }
            runCatching { AmarAiSelfImprovementScheduler.start(this@MainActivity) }
        }
        startMarketVisualSync()
        startAiCommandBridge()
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (showingRoom) showHome() else finish() }
        })
    }

    private fun startAiCommandBridge() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                AmarAiAppCommandBus.commands.collect { command ->
                    when (command) {
                        is AmarAiAppCommandBus.Command.OpenRoom -> showRoom(command.room)
                        is AmarAiAppCommandBus.Command.SetVisualEffects -> setVisualEffectsEnabled(command.enabled)
                        is AmarAiAppCommandBus.Command.QueueBotCommand -> runCatching { AmarBotCommandEngine(this@MainActivity).queue(command.botNumber, command.command) }
                    }
                }
            }
        }
    }

    private fun startMarketVisualSync() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    home?.let { web -> if (!showingRoom) runCatching { web.evaluateJavascript(AmarAiOrbMarketMotionController.javascript(AmarMarketStateStore.snapshot), null) } }
                    delay(750L)
                }
            }
        }
    }

    private fun installProtectionHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { AmarProtectionCenter.recordFailure(this, currentRoom?.titleAr ?: "بدء التشغيل", error) }
            previous?.uncaughtException(thread, error)
        }
    }

    private fun enterImmersiveReferenceMode() {
        if (Build.VERSION.SDK_INT >= 30) window.insetsController?.let { controller -> controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars()); controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE } else {
            @Suppress("DEPRECATION") window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private fun effectiveDark(): Boolean = when (themeMode) {
        AmarThemeMode.DARK -> true
        AmarThemeMode.LIGHT -> false
        AmarThemeMode.AUTO -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    private fun palette() = if (effectiveDark()) AmarPlatinum else AmarDay

    private fun enhanceHome() {
        // The new AMAR AI workspace owns its presentation. No legacy UI injection is performed here.
    }

    private fun applyHomeTheme() {
        home?.evaluateJavascript("window.setTheme && window.setTheme('${themeMode.name}')", null)
        home?.evaluateJavascript("window.setVisualEffectsEnabled && window.setVisualEffectsEnabled(${AmarGlobalVisualStateStore.current().enabled})", null)
    }

    private fun renderCurrentRoom() {
        val room = currentRoom ?: return
        roomHost?.setContent { AmarTheme(palette(), themeMode) { AmarRoomHostScreen(room, ::showHome, themeMode, ::onThemeModeChanged, homeLayout, ::onHomeLayoutChanged) } }
    }

    private fun showRoom(room: AmarRoom) { ensureRoomHost(); ensureVisualOverlay(); currentRoom = room; showingRoom = true; home?.visibility = android.view.View.GONE; roomHost?.visibility = android.view.View.VISIBLE; renderCurrentRoom() }
    private fun onThemeModeChanged(mode: AmarThemeMode) { themeMode = mode; prefs.edit().putString(AmarSharedUiContract.PREF_THEME_MODE, mode.name).apply(); applyHomeTheme(); renderCurrentRoom() }
    private fun onHomeLayoutChanged(layout: Int) { homeLayout = layout.coerceIn(AmarHomeLayoutController.DEFAULT_LAYOUT, AmarHomeLayoutController.LAYOUT_COUNT); prefs.edit().putInt(AmarSharedUiContract.PREF_HOME_LAYOUT, homeLayout).apply() }
    private fun showHome() { showingRoom = false; currentRoom = null; roomHost?.visibility = android.view.View.GONE; home?.visibility = android.view.View.VISIBLE; applyHomeTheme() }
    private fun setVisualEffectsEnabled(enabled: Boolean) { AmarVisualEffectsPreference.save(this, enabled); AmarGlobalVisualStateStore.setEnabled(enabled); home?.evaluateJavascript("window.setVisualEffectsEnabled && window.setVisualEffectsEnabled($enabled)", null) }

    private fun showStartupError(stage: String, error: Throwable) {
        runCatching {
            AmarProtectionCenter.recordFailure(this, stage, error)
            val message = buildString { append(error.javaClass.simpleName); if (!error.message.isNullOrBlank()) append("\n").append(error.message) }.take(260)
            val recovery = ComposeView(this).apply { setContent { Surface(Modifier.fillMaxSize(), color = Color(0xFF07121B)) { Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text("AMAR AI", color = Color(0xFF19E6FF), fontSize = 30.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(10.dp)); Text("تعذر تشغيل التطبيق", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Text(stage, color = Color(0xFFFFD36A), textAlign = TextAlign.Center); Spacer(Modifier.height(8.dp)); Text(message, color = Color(0xFF9DB5BF), fontSize = 11.sp, textAlign = TextAlign.Center) } } } }
            root.removeAllViews(); root.addView(recovery, FrameLayout.LayoutParams(-1, -1))
        }.onFailure { Log.e("AMAR_STARTUP", "Failed to render startup error", error) }
    }

    override fun onDestroy() {
        home?.let { web -> runCatching { root.removeView(web); web.stopLoading(); web.removeAllViews(); web.destroy() } }
        home = null
        super.onDestroy()
    }

    private inner class HomeBridge {
        @JavascriptInterface fun openRoom(name: String) { runOnUiThread { runCatching { AmarRoom.valueOf(name) }.getOrNull()?.let(::showRoom) } }
        @JavascriptInterface fun openHome() { runOnUiThread(::showHome) }
        @JavascriptInterface fun setVisualEffectsEnabled(enabled: Boolean) { runOnUiThread { this@MainActivity.setVisualEffectsEnabled(enabled) } }
        @JavascriptInterface fun ask(request: String) {
            lifecycleScope.launch {
                val result = runCatching {
                    val q = request.lowercase()
                    when {
                        listOf("سوق", "تحليل", "سعر", "market", "analysis").any(q::contains) -> AmarAiEngineBinding.market()
                        listOf("مخاطر", "risk", "حماية").any(q::contains) -> AmarAiEngineBinding.riskGate()
                        else -> "AMAR_UI_GATE|status=RECEIVED|authority=AMAR_AI_ENGINE|execution=DISABLED|verification=NOT_AVAILABLE|next=AGENT_STAGE_PIPELINE"
                    }
                }.getOrElse { "AMAR_UI_GATE|status=ERROR|failClosed=true|reason=${it.javaClass.simpleName}" }
                val safe = org.json.JSONObject.quote(result)
                runOnUiThread { home?.evaluateJavascript("window.receiveAgent && window.receiveAgent($safe,'تمت معالجة طلب الواجهة')", null) }
            }
        }
    }
}
