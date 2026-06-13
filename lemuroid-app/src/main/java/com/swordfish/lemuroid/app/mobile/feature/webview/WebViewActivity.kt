package com.swordfish.lemuroid.app.mobile.feature.webview

import android.content.Context
import android.content.Intent
import android.content.MutableContextWrapper
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.GetApp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.FulldiveConfigs
import com.swordfish.lemuroid.app.appextension.isRoomcordInstalled
import com.swordfish.lemuroid.app.appextension.openAppInGooglePlay
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme

class WebViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        val initialTitle = intent.getStringExtra(EXTRA_TITLE)

        setContent {
            AppTheme {
                WebViewScreen(
                    url = url,
                    initialTitle = initialTitle,
                    onNavigateBack = { finish() },
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Persist cookies to disk so the session (logged-in user) survives app restarts.
        CookieManager.getInstance().flush()
    }

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_TITLE = "extra_title"

        fun newIntent(
            context: Context,
            url: String,
            title: String? = null,
        ): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WebViewScreen(
    url: String,
    initialTitle: String?,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var title by remember { mutableStateOf(initialTitle.orEmpty()) }
    var isError by remember { mutableStateOf(false) }
    // If we are resuming a WebView that already loaded this URL, don't show the loading bar
    // (onPageFinished won't fire again, so it would otherwise stay visible forever).
    var isLoading by remember { mutableStateOf(RoomcordWebViewCache.loadedUrl != url) }
    // Defer WebView construction by one frame so the Scaffold (top bar + progress) draws first.
    // Creating a WebView the first time in a process is expensive and would otherwise block the
    // first frame, leaving the user staring at the empty window background for seconds.
    var webViewAttached by remember { mutableStateOf(false) }
    val fallbackTitle = stringResource(R.string.roomcord_webview_title)

    val showLoading = isLoading && !isError

    LaunchedEffect(Unit) {
        webViewAttached = true
    }

    BackHandler {
        val view = webView
        if (view != null && view.canGoBack()) {
            view.goBack()
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title.ifBlank { fallbackTitle },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    // If Roomcord is installed, offer to launch it; otherwise offer to install it.
                    val roomcordInstalled = remember { context.isRoomcordInstalled() }
                    IconButton(
                        onClick = {
                            if (roomcordInstalled) {
                                context.packageManager
                                    .getLaunchIntentForPackage(FulldiveConfigs.ROOMCORD_PACKAGE_NAME)
                                    ?.let { context.startActivity(it) }
                            } else {
                                context.openAppInGooglePlay(FulldiveConfigs.ROOMCORD_PACKAGE_NAME)
                            }
                        },
                    ) {
                        Icon(
                            imageVector =
                                if (roomcordInstalled) {
                                    Icons.AutoMirrored.Filled.Launch
                                } else {
                                    Icons.Outlined.GetApp
                                },
                            contentDescription =
                                stringResource(
                                    if (roomcordInstalled) {
                                        R.string.webview_launch_roomcord
                                    } else {
                                        R.string.webview_open_roomcord
                                    },
                                ),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            if (showLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (webViewAttached) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            obtainCachedWebView(ctx).apply {
                                // Rebind clients on every attach: they capture the current
                                // screen's state, which differs across Activity instances.
                                webViewClient =
                                    LemuroidWebViewClient(
                                        onStarted = {
                                            isError = false
                                            isLoading = true
                                        },
                                        onFinished = { isLoading = false },
                                        onError = {
                                            isError = true
                                            isLoading = false
                                        },
                                    )
                                webChromeClient =
                                    LemuroidWebChromeClient(
                                        onTitle = { newTitle ->
                                            if (initialTitle.isNullOrBlank()) {
                                                title = newTitle
                                            }
                                        },
                                    )
                                // Load only the first time (or when a different URL is requested);
                                // otherwise keep the existing page state (scroll, history, JS).
                                if (RoomcordWebViewCache.loadedUrl != url) {
                                    loadUrl(url)
                                    RoomcordWebViewCache.loadedUrl = url
                                }
                                webView = this
                            }
                        },
                        onRelease = { view ->
                            // Detach but DON'T destroy: keep the instance cached for next time.
                            (view.parent as? ViewGroup)?.removeView(view)
                            // Drop the Activity reference so it can be GC'd while cached.
                            RoomcordWebViewCache.contextWrapper?.baseContext =
                                view.context.applicationContext
                        },
                    )
                }

                if (isError) {
                    WebViewError(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                        onReload = {
                            isError = false
                            isLoading = true
                            webView?.reload()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun WebViewError(
    modifier: Modifier = Modifier,
    onReload: () -> Unit,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.webview_load_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReload) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
            )
            Spacer(Modifier.width(8.dp))
            Text(text = stringResource(R.string.webview_reload))
        }
    }
}

/**
 * Process-level cache of a single WebView so reopening the screen resumes the previous state
 * (scroll position, in-page history, JS) instead of recreating and reloading the page.
 * The instance is kept alive for the whole process; its context is a [MutableContextWrapper]
 * whose base is swapped to the current Activity while attached and back to the application
 * context when detached, so the Activity is never leaked.
 */
private object RoomcordWebViewCache {
    var webView: WebView? = null
    var contextWrapper: MutableContextWrapper? = null
    var loadedUrl: String? = null
}

private fun obtainCachedWebView(activityContext: Context): WebView {
    RoomcordWebViewCache.webView?.let { existing ->
        RoomcordWebViewCache.contextWrapper?.baseContext = activityContext
        (existing.parent as? ViewGroup)?.removeView(existing)
        return existing
    }

    val wrapper = MutableContextWrapper(activityContext)
    val webView =
        WebView(wrapper).apply {
            layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            settings.javaScriptEnabled = true
            // Persist localStorage / sessionStorage across launches.
            settings.domStorageEnabled = true
            // Use the HTTP cache according to response headers.
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            // Keep the logged-in session: accept cookies (incl. cross-domain auth flows)
            // so the same user persists between visits.
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        }
    RoomcordWebViewCache.contextWrapper = wrapper
    RoomcordWebViewCache.webView = webView
    return webView
}

private class LemuroidWebViewClient(
    private val onStarted: () -> Unit,
    private val onFinished: () -> Unit,
    private val onError: () -> Unit,
) : WebViewClient() {
    override fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: Bitmap?,
    ) {
        onStarted()
    }

    override fun onPageFinished(
        view: WebView?,
        url: String?,
    ) {
        onFinished()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        if (request?.isForMainFrame == true) {
            onError()
        }
    }
}

private class LemuroidWebChromeClient(
    private val onTitle: (String) -> Unit,
) : WebChromeClient() {
    override fun onReceivedTitle(
        view: WebView?,
        title: String?,
    ) {
        if (!title.isNullOrBlank()) {
            onTitle(title)
        }
    }
}
