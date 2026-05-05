package com.lockedbrowser.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(entry: HistoryEntry)

    // Returns all history, newest first - NO DELETE method by design
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 500")
    suspend fun getAllOnce(): List<HistoryEntry>
}
