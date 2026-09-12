package com.personal.gridbot

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.personal.gridbot.amaros.ai.AmarAiAppCommandBus
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
    private lateinit var home: WebView
    private lateinit var roomHost: ComposeView
    private lateinit var visualOverlay: ComposeView
    private var showingRoom = false
    private var currentRoom: AmarRoom? = null
    private var themeMode by mutableStateOf(AmarThemeMode.DARK)
    private var homeLayout by mutableStateOf(AmarHomeLayoutController.DEFAULT_LAYOUT)
    private val prefs by lazy { getSharedPreferences(AmarSharedUiContract.PREFS, MODE_PRIVATE) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeMode = runCatching { AmarThemeMode.valueOf(prefs.getString(AmarSharedUiContract.PREF_THEME_MODE, AmarThemeMode.DARK.name) ?: AmarThemeMode.DARK.name) }.getOrDefault(AmarThemeMode.DARK)
        homeLayout = prefs.getInt(AmarSharedUiContract.PREF_HOME_LAYOUT, AmarHomeLayoutController.DEFAULT_LAYOUT).coerceIn(AmarHomeLayoutController.DEFAULT_LAYOUT, AmarHomeLayoutController.LAYOUT_COUNT)
        AmarGlobalVisualStateStore.setEnabled(AmarVisualEffectsPreference.load(this))
        AmarTradingDiscoveryScheduler.start(this)
        installProtectionHandler()
        enterImmersiveReferenceMode()
        root = FrameLayout(this)
        home = WebView(this).apply {
            webViewClient = object : WebViewClient() { override fun onPageFinished(view: WebView?, url: String?) { enhanceHome(); applyHomeTheme() } }
            settings.javaScriptEnabled = true; settings.domStorageEnabled = true; settings.cacheMode = WebSettings.LOAD_DEFAULT; settings.allowFileAccess = true; settings.allowContentAccess = false; settings.builtInZoomControls = false; settings.displayZoomControls = false
            addJavascriptInterface(HomeBridge(), "Android"); loadUrl("file:///android_asset/amar_reference.html")
        }
        roomHost = ComposeView(this).apply { visibility = android.view.View.GONE }
        visualOverlay = ComposeView(this).apply { setContent { AmarGlobalVisualOverlay(this@MainActivity) } }
        root.addView(home, FrameLayout.LayoutParams(-1, -1)); root.addView(roomHost, FrameLayout.LayoutParams(-1, -1)); root.addView(visualOverlay, FrameLayout.LayoutParams(-1, -1)); setContentView(root)
        startMarketVisualSync()
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
                    if (!showingRoom && ::home.isInitialized) {
                        val js = AmarAiOrbMarketMotionController.javascript(AmarMarketStateStore.snapshot)
                        home.evaluateJavascript(js, null)
                    }
                    delay(750L)
                }
            }
        }
    }

    private fun installProtectionHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { AmarProtectionCenter.recordFailure(this, currentRoom?.titleAr ?: "التطبيق", error) }
            previous?.uncaughtException(thread, error)
        }
    }

    private fun enterImmersiveReferenceMode() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            val flags = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = flags
        }
    }

    private fun effectiveDark(): Boolean = when (themeMode) {
        AmarThemeMode.DARK -> true
        AmarThemeMode.LIGHT -> false
        AmarThemeMode.AUTO -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun palette() = if (effectiveDark()) AmarPlatinum else AmarDay

    private fun enhanceHome() {
        val js = """
        (function(){
          if(window.__amarEnhanced)return; window.__amarEnhanced=true;
          if(!document.getElementById('amarNewsGate')){
            var g=document.createElement('button');g.id='amarNewsGate';g.type='button';g.textContent='غرفة\\nالأخبار\\nوالجلسات';g.setAttribute('aria-label','غرفة الأخبار والجلسات');g.onclick=function(){if(window.Android&&Android.openRoom)Android.openRoom('NEWS_SESSIONS')};g.style.cssText='position:absolute;right:50%;top:19%;transform:translateX(50%);z-index:11;width:66px;height:66px;border-radius:50%;background:radial-gradient(circle at 35% 30%,#fff3b0 0%,#d9a63b 19%,#8d5f1d 35%,#101b23 63%,#05070b 100%);border:1px solid #e6ba52;box-shadow:0 0 0 3px rgba(216,168,58,.13),0 0 20px rgba(0,217,255,.28),0 0 38px rgba(255,190,70,.16);color:#fff4b0;font-size:8px;font-weight:1000;white-space:pre-line;animation:amarNewsFloat 4.8s ease-in-out infinite,amarNewsPulse 2.2s ease-in-out infinite;transition:filter .2s ease,scale .2s ease;cursor:pointer';document.querySelector('.scene').appendChild(g);var st=document.createElement('style');st.textContent='@keyframes amarNewsFloat{0%,100%{translate:0 0 rotate(0deg)}50%{translate:0 -7px rotate(2deg)}}@keyframes amarNewsPulse{0%,100%{filter:brightness(1)}50%{filter:brightness(1.2)}}';document.head.appendChild(st);
          }
        })();
        """.trimIndent()
        home.evaluateJavascript(js, null)
    }

    private fun applyHomeTheme() { home.evaluateJavascript("window.setTheme && window.setTheme('${themeMode.name}')", null); home.evaluateJavascript("window.setVisualEffectsEnabled && window.setVisualEffectsEnabled(${AmarGlobalVisualStateStore.current().enabled})", null); applyHomeLayout() }
    private fun applyHomeLayout() { home.evaluateJavascript(AmarHomeLayoutController.applyJavascript(homeLayout), null) }
    private fun renderCurrentRoom() { currentRoom?.let { roomHost.setContent { AmarTheme(palette(), themeMode) { AmarRoomHostScreen(it, ::showHome, themeMode, ::onThemeModeChanged, homeLayout, ::onHomeLayoutChanged) } } } }
    private fun showRoom(room: AmarRoom) { currentRoom = room; showingRoom = true; home.visibility = android.view.View.GONE; roomHost.visibility = android.view.View.VISIBLE; renderCurrentRoom() }
    private fun onThemeModeChanged(mode: AmarThemeMode) { themeMode = mode; prefs.edit().putString(AmarSharedUiContract.PREF_THEME_MODE, mode.name).apply(); applyHomeTheme(); renderCurrentRoom() }
    private fun onHomeLayoutChanged(layout: Int) { homeLayout = layout.coerceIn(AmarHomeLayoutController.DEFAULT_LAYOUT, AmarHomeLayoutController.LAYOUT_COUNT); prefs.edit().putInt(AmarSharedUiContract.PREF_HOME_LAYOUT, homeLayout).apply(); if(!showingRoom){applyHomeLayout()} else {showHome();applyHomeLayout()} }
    private fun showHome() { showingRoom = false; currentRoom = null; roomHost.visibility = android.view.View.GONE; home.visibility = android.view.View.VISIBLE; applyHomeTheme() }
    private fun setVisualEffectsEnabled(enabled: Boolean) { AmarVisualEffectsPreference.save(this, enabled); AmarGlobalVisualStateStore.setEnabled(enabled); home.evaluateJavascript("window.setVisualEffectsEnabled && window.setVisualEffectsEnabled($enabled)", null) }
    private inner class HomeBridge { @JavascriptInterface fun openRoom(name: String) { runOnUiThread { runCatching { AmarRoom.valueOf(name) }.getOrNull()?.let(::showRoom) } }; @JavascriptInterface fun openHome() { runOnUiThread(::showHome) }; @JavascriptInterface fun setVisualEffectsEnabled(enabled: Boolean) { runOnUiThread { this@MainActivity.setVisualEffectsEnabled(enabled) } } }
}
