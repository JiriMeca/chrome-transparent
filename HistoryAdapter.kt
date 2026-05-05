package com.lockedbrowser.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lockedbrowser.app.data.HistoryEntry
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private var items: List<HistoryEntry>,
    private val onClick: (HistoryEntry) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("d. M. yyyy HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvUrl: TextView = view.findViewById(R.id.tvUrl)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = items[position]
        holder.tvTitle.text = entry.title
        holder.tvUrl.text = entry.url
        holder.tvTime.text = dateFormat.format(Date(entry.timestamp))
        holder.itemView.setOnClickListener { onClick(entry) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<HistoryEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}
