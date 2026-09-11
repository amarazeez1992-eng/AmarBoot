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
import com.personal.gridbot.amaros.design.AmarHomeLayoutController
import com.personal.gridbot.amaros.design.AmarSharedUiContract
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
    private var homeLayout by mutableStateOf(AmarHomeLayoutController.DEFAULT_LAYOUT)
    private val prefs by lazy { getSharedPreferences(AmarSharedUiContract.PREFS, MODE_PRIVATE) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeMode = runCatching { AmarThemeMode.valueOf(prefs.getString(AmarSharedUiContract.PREF_THEME_MODE, AmarThemeMode.DARK.name) ?: AmarThemeMode.DARK.name) }.getOrDefault(AmarThemeMode.DARK)
        homeLayout = prefs.getInt(AmarSharedUiContract.PREF_HOME_LAYOUT, AmarHomeLayoutController.DEFAULT_LAYOUT).coerceIn(AmarHomeLayoutController.DEFAULT_LAYOUT, AmarHomeLayoutController.LAYOUT_COUNT)
        AmarGlobalVisualStateStore.setEnabled(AmarVisualEffectsPreference.load(this))
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
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) { override fun handleOnBackPressed() { if (showingRoom) showHome() else finish() } })
    }

    private fun installProtectionHandler() { val previous = Thread.getDefaultUncaughtExceptionHandler(); Thread.setDefaultUncaughtExceptionHandler { thread, error -> runCatching { AmarProtectionCenter.recordFailure(this, currentRoom?.titleAr ?: "التطبيق", error) }; previous?.uncaughtException(thread, error) } }
    private fun enterImmersiveReferenceMode() { @Suppress("DEPRECATION") window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE; if (android.os.Build.VERSION.SDK_INT >= 30) window.insetsController?.let { it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()); it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE } }
    private fun effectiveDark(): Boolean = when (themeMode) { AmarThemeMode.DARK -> true; AmarThemeMode.LIGHT -> false; AmarThemeMode.AUTO -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES }
    private fun palette() = if (effectiveDark()) AmarPlatinum else AmarDay

    private fun enhanceHome() {
        val js = """
        (function(){
          if(window.__amarEnhanced)return; window.__amarEnhanced=true;
          var c=document.getElementById('clock');
          if(c){c.className='analog';c.innerHTML='<i id=ah></i><i id=am></i><i id=as></i><b></b>';c.style.cssText='position:relative;width:52px;height:52px;border-radius:50%;background:#fff;border:3px solid #d8a83a;box-shadow:0 0 0 2px #111,0 0 18px rgba(216,168,58,.2);direction:ltr';['ah','am','as'].forEach(function(id,i){var e=document.getElementById(id);e.style.cssText='position:absolute;left:50%;bottom:50%;transform-origin:50% 100%;transform:translateX(-50%) rotate(0deg);border-radius:5px;background:'+(i==2?'#d8a83a':'#111')+';width:'+(i==0?'3px':i==1?'2px':'1px')+';height:'+(i==0?'15px':i==1?'20px':'22px')});var p=c.querySelector('b');p.style.cssText='position:absolute;width:5px;height:5px;border-radius:50%;background:#d8a83a;left:50%;top:50%;transform:translate(-50%,-50%)';setInterval(function(){var d=new Date(),h=d.getHours()%12,m=d.getMinutes(),s=d.getSeconds();document.getElementById('ah').style.transform='translateX(-50%) rotate('+(h*30+m*.5)+'deg)';document.getElementById('am').style.transform='translateX(-50%) rotate('+(m*6+s*.1)+'deg)';document.getElementById('as').style.transform='translateX(-50%) rotate('+(s*6)+'deg)'},1000)}
          if(!document.getElementById('amarNewsGate')){var g=document.createElement('button');g.id='amarNewsGate';g.textContent='غرفة\\nالأخبار\\nوالجلسات';g.onclick=function(){if(window.Android&&Android.openRoom)Android.openRoom('NEWS_SESSIONS')};g.style.cssText='position:absolute;right:50%;top:19%;transform:translateX(50%);z-index:11;width:66px;height:66px;border-radius:50%;background:radial-gradient(circle at 35% 30%,#fff3b0,#c89425 20%,#101b23 62%,#05070b);border:1px solid #d8a83a;box-shadow:0 0 0 3px rgba(216,168,58,.12),0 0 28px rgba(0,217,255,.24);color:#fff4b0;font-size:8px;font-weight:1000;white-space:pre-line;animation:amarGateMove 11s ease-in-out infinite,amarGatePulse 2.4s ease-in-out infinite';document.querySelector('.scene').appendChild(g);var st=document.createElement('style');st.textContent='@keyframes amarGateMove{0%,100%{translate:0 0}25%{translate:-25vw 4vh}50%{translate:22vw 9vh}75%{translate:-14vw 2vh}}@keyframes amarGatePulse{50%{scale:1.08;filter:brightness(1.2)}}';document.head.appendChild(st)}
          if(!document.getElementById('amarAiOrb')){var a=document.createElement('button');a.id='amarAiOrb';a.setAttribute('aria-label','AMAR AI');a.innerHTML='<span>AI</span><i></i>';a.onclick=function(){if(window.Android&&Android.openRoom)Android.openRoom('ANALYSIS')};a.style.cssText='position:fixed;left:7vw;bottom:7vh;z-index:30;width:76px;height:76px;border-radius:50%;border:1px solid rgba(255,255,255,.72);background:radial-gradient(circle at 32% 25%,#ffffff,#bff8ff 18%,#62b8ff 42%,#6847d8 68%,#24104c);box-shadow:0 0 0 3px rgba(117,220,255,.22),0 0 34px rgba(93,198,255,.7),inset 0 0 22px rgba(255,255,255,.55);color:#fff;font:900 18px sans-serif;cursor:pointer;animation:amarAiOrbit 16s ease-in-out infinite,amarAiSpin 7s linear infinite;overflow:hidden}';var as=document.createElement('style');as.textContent='@keyframes amarAiOrbit{0%,100%{left:7vw;bottom:7vh;transform:rotate(0deg) scale(1)}12%{left:25vw;bottom:5vh;transform:rotate(45deg) scale(1.03)}25%{left:49vw;bottom:10vh;transform:rotate(90deg) scale(.96)}38%{left:72vw;bottom:6vh;transform:rotate(135deg) scale(1.04)}50%{left:86vw;bottom:28vh;transform:rotate(180deg) scale(1)}62%{left:73vw;bottom:57vh;transform:rotate(225deg) scale(1.05)}75%{left:45vw;bottom:70vh;transform:rotate(270deg) scale(.97)}88%{left:18vw;bottom:48vh;transform:rotate(315deg) scale(1.04)}}@keyframes amarAiSpin{to{filter:hue-rotate(360deg) brightness(1.12)}}#amarAiOrb span{position:absolute;inset:0;display:grid;place-items:center;text-shadow:0 0 12px #fff;z-index:2}#amarAiOrb i{position:absolute;width:22px;height:22px;left:9px;top:8px;border-radius:50%;background:rgba(255,255,255,.9);filter:blur(2px);opacity:.85;animation:amarAiShine 2.2s ease-in-out infinite}@keyframes amarAiShine{50%{left:48px;top:49px;opacity:.15}}';document.head.appendChild(as);document.querySelector('.scene').appendChild(a)}
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
