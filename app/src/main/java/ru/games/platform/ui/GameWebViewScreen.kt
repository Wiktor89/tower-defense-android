package ru.games.platform.ui

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import ru.games.platform.data.api.User

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GameWebViewScreen(
    title: String,
    baseUrl: String,
    gamePath: String,
    user: User,
    onBack: () -> Unit,
) {
    val gson = remember { Gson() }
    val root = remember(baseUrl) { baseUrl.trimEnd('/') }
    val gameUrl = remember(root, gamePath) {
        val path = if (gamePath.startsWith("/")) gamePath else "/$gamePath"
        root + path
    }
    val injectJs = remember(user) {
        val safe = gson.toJson(user.copy(adminToken = null))
            .replace("\\", "\\\\")
            .replace("'", "\\'")
        "try{localStorage.setItem('games_user','$safe');}catch(e){}"
    }

    var webViewRef: WebView? = null

    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) wv.goBack() else onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    webViewRef = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    webChromeClient = WebChromeClient()

                    var bootstrapped = false
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean = false

                        override fun onPageFinished(view: WebView?, url: String?) {
                            view ?: return
                            view.evaluateJavascript(injectJs, null)
                            if (!bootstrapped && url != null && !url.contains("/games/")) {
                                bootstrapped = true
                                view.loadUrl(gameUrl)
                            }
                        }
                    }
                    // Сначала origin сервера (чтобы localStorage был на нужном host), потом игра
                    loadUrl("$root/")
                }
            },
            update = { webViewRef = it },
        )
    }
}
