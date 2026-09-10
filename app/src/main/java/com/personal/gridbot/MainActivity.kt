package com.personal.gridbot

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.setContentView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import com.personal.gridbot.amaros.navigation.AmarRoom
import com.personal.gridbot.amaros.navigation.AmarRoomHostScreen

class MainActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private lateinit var home: WebView
    private lateinit var roomHost: ComposeView
    private var showingRoom = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = FrameLayout(this)
        home = WebView(this).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            addJavascriptInterface(HomeBridge(), "Android")
            loadUrl("file:///android_asset/amar_reference.html")
        }

        roomHost = ComposeView(this).apply {
            visibility = android.view.View.GONE
        }

        root.addView(home, FrameLayout.LayoutParams(-1, -1))
        root.addView(roomHost, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (showingRoom) showHome() else finish()
            }
        })
    }

    private fun showRoom(room: AmarRoom) {
        showingRoom = true
        home.visibility = android.view.View.GONE
        roomHost.visibility = android.view.View.VISIBLE
        roomHost.setContent {
            MaterialTheme {
                AmarRoomHostScreen(
                    room = room,
                    onBackHome = ::showHome
                )
            }
        }
    }

    private fun showHome() {
        showingRoom = false
        roomHost.visibility = android.view.View.GONE
        home.visibility = android.view.View.VISIBLE
    }

    private inner class HomeBridge {
        @JavascriptInterface
        fun openRoom(name: String) {
            runOnUiThread {
                val room = runCatching { AmarRoom.valueOf(name) }.getOrNull()
                if (room != null) showRoom(room)
            }
        }

        @JavascriptInterface
        fun openHome() {
            runOnUiThread(::showHome)
        }
    }
}
