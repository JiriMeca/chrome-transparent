package com.lockedbrowser.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lockedbrowser.app.databinding.ActivityBookmarksBinding

class BookmarksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarksBinding
    private lateinit var adapter: BookmarkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = BookmarkAdapter(
            items = BookmarkManager.getAll(this).toMutableList(),
            onClick = { bookmark ->
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("url", bookmark.url)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            },
            onDelete = { bookmark ->
                BookmarkManager.remove(this, bookmark)
                adapter.remove(bookmark)
                updateEmptyState()
                Toast.makeText(this, getString(R.string.bookmark_removed), Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerBookmarks.layoutManager = LinearLayoutManager(this)
        binding.recyclerBookmarks.adapter = adapter

        // Add current page as bookmark
        binding.fabAddBookmark.setOnClickListener {
            val url = MainActivity.currentUrl
            val title = url.removePrefix("https://").removePrefix("http://").split("/").first()
            val bookmark = Bookmark(title = title, url = url)
            BookmarkManager.add(this, bookmark)
            adapter.addItem(bookmark)
            updateEmptyState()
            Toast.makeText(this, getString(R.string.bookmark_added), Toast.LENGTH_SHORT).show()
        }

        updateEmptyState()
    }

    private fun updateEmptyState() {
        binding.tvEmpty.visibility =
            if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }
}

class BookmarkAdapter(
    private val items: MutableList<Bookmark>,
    private val onClick: (Bookmark) -> Unit,
    private val onDelete: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvBookmarkTitle)
        val tvUrl: TextView = view.findViewById(R.id.tvBookmarkUrl)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bookmark, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvUrl.text = item.url
        holder.itemView.setOnClickListener { onClick(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size

    fun addItem(bookmark: Bookmark) {
        if (items.none { it.url == bookmark.url }) {
            items.add(0, bookmark)
            notifyItemInserted(0)
        }
    }

    fun remove(bookmark: Bookmark) {
        val idx = items.indexOfFirst { it.url == bookmark.url }
        if (idx >= 0) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
