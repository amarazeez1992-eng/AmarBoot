package com.personal.gridbot

import android.annotation.SuppressLint
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
import com.personal.gridbot.ui.theme.AmarDay
import com.personal.gridbot.ui.theme.AmarPlatinum
import com.personal.gridbot.ui.theme.AmarTheme
import com.personal.gridbot.ui.theme.AmarThemeMode

class MainActivity : ComponentActivity() {
    private lateinit var root: FrameLayout; private lateinit var home: WebView; private lateinit var roomHost: ComposeView
    private var showingRoom = false; private var currentRoom: AmarRoom? = null; private var themeMode by mutableStateOf(AmarThemeMode.DARK)
    private val prefs by lazy { getSharedPreferences("amar_ui", MODE_PRIVATE) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeMode = runCatching { AmarThemeMode.valueOf(prefs.getString("theme_mode", AmarThemeMode.DARK.name) ?: AmarThemeMode.DARK.name) }.getOrDefault(AmarThemeMode.DARK)
        enterImmersiveReferenceMode(); root = FrameLayout(this)
        home = WebView(this).apply {
            webViewClient = object : WebViewClient() { override fun onPageFinished(view: WebView?, url: String?) { applyHomeTheme() } }
            settings.javaScriptEnabled=true; settings.domStorageEnabled=true; settings.cacheMode=WebSettings.LOAD_DEFAULT; settings.allowFileAccess=true; settings.allowContentAccess=false; settings.builtInZoomControls=false; settings.displayZoomControls=false
            addJavascriptInterface(HomeBridge(), "Android"); loadUrl("file:///android_asset/amar_reference.html")
        }
        roomHost = ComposeView(this).apply { visibility = android.view.View.GONE }
        root.addView(home, FrameLayout.LayoutParams(-1,-1)); root.addView(roomHost, FrameLayout.LayoutParams(-1,-1)); setContentView(root)
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) { override fun handleOnBackPressed(){if(showingRoom)showHome() else finish()} })
    }

    private fun enterImmersiveReferenceMode(){
        @Suppress("DEPRECATION") window.decorView.systemUiVisibility=android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if(android.os.Build.VERSION.SDK_INT>=30) window.insetsController?.let{it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars());it.systemBarsBehavior=WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE}
    }
    private fun palette()=if(themeMode==AmarThemeMode.LIGHT)AmarDay else AmarPlatinum
    private fun applyHomeTheme(){home.evaluateJavascript("window.setTheme && window.setTheme('${themeMode.name}')",null)}
    private fun renderCurrentRoom(){currentRoom?.let{roomHost.setContent{AmarTheme(palette(),themeMode){AmarRoomHostScreen(it,::showHome,themeMode,::onThemeModeChanged)}}}}
    private fun showRoom(room:AmarRoom){currentRoom=room;showingRoom=true;home.visibility=android.view.View.GONE;roomHost.visibility=android.view.View.VISIBLE;renderCurrentRoom()}
    private fun onThemeModeChanged(mode:AmarThemeMode){themeMode=mode;prefs.edit().putString("theme_mode",mode.name).apply();applyHomeTheme();renderCurrentRoom()}
    private fun showHome(){showingRoom=false;currentRoom=null;roomHost.visibility=android.view.View.GONE;home.visibility=android.view.View.VISIBLE;applyHomeTheme()}
    private inner class HomeBridge{
        @JavascriptInterface fun openRoom(name:String){runOnUiThread{runCatching{AmarRoom.valueOf(name)}.getOrNull()?.let(::showRoom)}}
        @JavascriptInterface fun openHome(){runOnUiThread(::showHome)}
    }
}
