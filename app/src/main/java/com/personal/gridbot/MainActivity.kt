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
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.design.AmarAiOrbMarketMotionController
import com.personal.gridbot.amaros.design.AmarHomeLayoutController
import com.personal.gridbot.amaros.design.AmarSharedUiContract
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingDiscoveryScheduler
import com.personal.gridbot.amaros.navigation.AmarRoom
import com.personal.gridbot.amaros.navigation.AmarRoomHostScreen
import com.personal.gridbot.amaros.security.AmarProtectionCenter
import com.personal.gridbot.amaros.visual.AmarGlobalVisualOverlay
import com.personal.gridbot.amaros.visual.AmarVisualEffectsPreference
import com.personal.gridbot.amaros.visual.AmarGlobalVisualStateStore
import com.personal.gridbot.ui.theme.AmarDay
import com.personal.gridbot.ui.theme.AmarPlatinum
import com.personal.gridbot.ui.theme.AmarTheme
import com.personal.gridbot.ui.theme.AmarThemeMode
import kotlinx.coroutines.delay
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
        installProtectionHandler(); enterImmersiveReferenceMode()
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
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) { override fun handleOnBackPressed() { if (showingRoom) showHome() else finish() } })
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

    private fun installProtectionHandler() { AmarProtectionCenter.initialize(this) }
    private fun enterImmersiveReferenceMode() { window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()) }
    private fun enhanceHome() {}
    private fun applyHomeTheme() {}
    private fun showHome() { showingRoom = false; roomHost.visibility = android.view.View.GONE; home.visibility = android.view.View.VISIBLE }

    private inner class HomeBridge {
        @JavascriptInterface fun openRoom(name: String) { runOnUiThread { currentRoom = AmarRoom.entries.firstOrNull { it.name == name }; showingRoom = currentRoom != null; home.visibility = android.view.View.GONE; roomHost.visibility = android.view.View.VISIBLE; roomHost.setContent { AmarTheme(themeMode) { currentRoom?.let { AmarRoomHostScreen(it) } } } } }
    }
}
