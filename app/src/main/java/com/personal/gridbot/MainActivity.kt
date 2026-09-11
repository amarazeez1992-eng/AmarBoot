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

class MainActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private lateinit var home: WebView
    private lateinit var roomHost: ComposeView
    private lateinit var visualOverlay: ComposeView
    private var showingRoom = false
    private var currentRoom: AmarRoom? = null
    private var themeMode by mutableStateOf(AmarThemeMode.DARK)
    private val prefs by lazy { getSharedPreferences("amar_ui", MODE_PRIVATE) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeMode = runCatching { AmarThemeMode.valueOf(prefs.getString("theme_mode", AmarThemeMode.DARK.name) ?: AmarThemeMode.DARK.name) }.getOrDefault(AmarThemeMode.DARK)
        AmarGlobalVisualStateStore.setEnabled(AmarVisualEffectsPreference.load(this))
        installProtectionHandler()
        enterImmersiveReferenceMode()
        root = FrameLayout(this)
        home = WebView(this).apply {
            webViewClient = object : WebViewClient() { override fun onPageFinished(view: WebView?, url: String?) { applyHomeTheme() } }
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            addJavascriptInterface(HomeBridge(), "Android")
            loadUrl("file:///android_asset/amar_reference.html")
        }
        roomHost = ComposeView(this).apply { visibility = android.view.View.GONE }
        visualOverlay = ComposeView(this).apply { setContent { AmarGlobalVisualOverlay(this@MainActivity) } }
        root.addView(home, FrameLayout.LayoutParams(-1, -1))
        root.addView(roomHost, FrameLayout.LayoutParams(-1, -1))
        root.addView(visualOverlay, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) { override fun handleOnBackPressed() { if (showingRoom) showHome() else finish() } })
    }

    private fun installProtectionHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { AmarProtectionCenter.recordFailure(this, currentRoom?.titleAr ?: "التطبيق", error) }
            previous?.uncaughtException(thread, error)
        }
    }

    private fun enterImmersiveReferenceMode() {
        @Suppress("DEPRECATION") window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (android.os.Build.VERSION.SDK_INT >= 30) window.insetsController?.let { it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()); it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE }
    }

    private fun effectiveDark(): Boolean = when (themeMode) {
        AmarThemeMode.DARK -> true
        AmarThemeMode.LIGHT -> false
        AmarThemeMode.AUTO -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    private fun palette() = if (effectiveDark()) AmarPlatinum else AmarDay
    private fun applyHomeTheme() {
        home.evaluateJavascript("window.setTheme && window.setTheme('${themeMode.name}')", null)
        home.evaluateJavascript("window.setVisualEffectsEnabled && window.setVisualEffectsEnabled(${AmarGlobalVisualStateStore.current().enabled})", null)
    }
    private fun renderCurrentRoom() { currentRoom?.let { roomHost.setContent { AmarTheme(palette(), themeMode) { AmarRoomHostScreen(it, ::showHome, themeMode, ::onThemeModeChanged) } } } }
    private fun showRoom(room: AmarRoom) { currentRoom = room; showingRoom = true; home.visibility = android.view.View.GONE; roomHost.visibility = android.view.View.VISIBLE; renderCurrentRoom() }
    private fun onThemeModeChanged(mode: AmarThemeMode) { themeMode = mode; prefs.edit().putString("theme_mode", mode.name).apply(); applyHomeTheme(); renderCurrentRoom() }
    private fun showHome() { showingRoom = false; currentRoom = null; roomHost.visibility = android.view.View.GONE; home.visibility = android.view.View.VISIBLE; applyHomeTheme() }
    private fun setVisualEffectsEnabled(enabled: Boolean) { AmarVisualEffectsPreference.save(this, enabled); AmarGlobalVisualStateStore.setEnabled(enabled); home.evaluateJavascript("window.setVisualEffectsEnabled && window.setVisualEffectsEnabled($enabled)", null) }

    private inner class HomeBridge {
        @JavascriptInterface fun openRoom(name: String) { runOnUiThread { runCatching { AmarRoom.valueOf(name) }.getOrNull()?.let(::showRoom) } }
        @JavascriptInterface fun openHome() { runOnUiThread(::showHome) }
        @JavascriptInterface fun setVisualEffectsEnabled(enabled: Boolean) { runOnUiThread { this@MainActivity.setVisualEffectsEnabled(enabled) } }
    }
}
