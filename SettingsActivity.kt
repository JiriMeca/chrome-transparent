package com.lockedbrowser.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.lockedbrowser.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Search engine selector
        val engines = arrayOf("Google", "Bing", "DuckDuckGo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, engines)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSearchEngine.adapter = adapter

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        binding.spinnerSearchEngine.setSelection(prefs.getInt("search_engine", 0))

        binding.btnSave.setOnClickListener {
            prefs.edit()
                .putInt("search_engine", binding.spinnerSearchEngine.selectedItemPosition)
                .apply()
            finish()
        }

        // NOTE: There is intentionally NO "Clear History" button here.
    }

    companion object {
        fun getSearchUrl(context: android.content.Context, query: String): String {
            val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
            return when (prefs.getInt("search_engine", 0)) {
                1 -> "https://www.bing.com/search?q=${android.net.Uri.encode(query)}"
                2 -> "https://duckduckgo.com/?q=${android.net.Uri.encode(query)}"
                else -> "https://www.google.com/search?q=${android.net.Uri.encode(query)}"
            }
        }
    }
}
