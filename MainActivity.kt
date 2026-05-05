package com.lockedbrowser.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lockedbrowser.app.data.AppDatabase
import com.lockedbrowser.app.data.HistoryEntry
import com.lockedbrowser.app.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase

    companion object {
        const val HOMEPAGE = "https://www.google.com"
        var currentUrl: String = HOMEPAGE
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupWebView()
        setupUrlBar()
        setupButtons()
        setupBottomNav()

        binding.webView.loadUrl(HOMEPAGE)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            allowFileAccess = false
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.isIndeterminate = false
                url?.let {
                    binding.etUrl.setText(it)
                    currentUrl = it
                    binding.lockIcon.visibility =
                        if (it.startsWith("https")) View.VISIBLE else View.GONE
                }
                updateNavButtons()
            }

            override fun onPageFinished(view: WebView, url: String?) {
                binding.progressBar.visibility = View.GONE
                url?.let {
                    binding.etUrl.setText(it)
                    currentUrl = it
                    saveToHistory(it, view.title ?: it)
                }
                updateNavButtons()
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                binding.progressBar.progress = newProgress
                binding.progressBar.visibility =
                    if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupUrlBar() {
        binding.etUrl.setOnEditorActionListener { _, actionId, event ->
            val isGo = actionId == EditorInfo.IME_ACTION_GO
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                    event.action == KeyEvent.ACTION_DOWN
            if (isGo || isEnter) {
                navigateToUrl(binding.etUrl.text.toString().trim())
                true
            } else false
        }

        binding.etUrl.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.etUrl.selectAll()
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) binding.webView.goBack()
        }
        binding.btnForward.setOnClickListener {
            if (binding.webView.canGoForward()) binding.webView.goForward()
        }
        binding.btnRefresh.setOnClickListener {
            binding.webView.reload()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    binding.webView.loadUrl(HOMEPAGE)
                    true
                }
                R.id.nav_bookmarks -> {
                    startActivity(Intent(this, BookmarksActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToUrl(input: String) {
        val url = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") && !input.contains(" ") -> "https://$input"
            else -> "https://www.google.com/search?q=${Uri.encode(input)}"
        }
        binding.webView.loadUrl(url)
        hideKeyboard()
    }

    private fun saveToHistory(url: String, title: String) {
        if (url.startsWith("http")) {
            lifecycleScope.launch(Dispatchers.IO) {
                db.historyDao().insert(
                    HistoryEntry(
                        url = url,
                        title = title.ifEmpty { url },
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun updateNavButtons() {
        binding.btnBack.alpha = if (binding.webView.canGoBack()) 1.0f else 0.35f
        binding.btnForward.alpha = if (binding.webView.canGoForward()) 1.0f else 0.35f
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etUrl.windowToken, 0)
        binding.etUrl.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Called from BookmarksActivity to open a URL
    fun openUrl(url: String) {
        binding.webView.loadUrl(url)
    }
}
