package com.lockedbrowser.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.lockedbrowser.app.data.AppDatabase
import com.lockedbrowser.app.databinding.ActivityHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = HistoryAdapter(emptyList()) { entry ->
            // Open URL in main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("url", entry.url)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                db.historyDao().getAllOnce()
            }
            if (items.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerHistory.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerHistory.visibility = View.VISIBLE
                adapter.updateData(items)
            }
        }
    }
}
