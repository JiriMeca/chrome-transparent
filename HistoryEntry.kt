package com.lockedbrowser.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val timestamp: Long
)
