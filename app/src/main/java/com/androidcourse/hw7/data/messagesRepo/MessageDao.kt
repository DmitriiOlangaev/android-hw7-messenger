package com.androidcourse.hw7.data.messagesRepo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE channel = :channel AND id > :lastKnownId ORDER BY id ASC LIMIT :fetchMessagesCount")
    suspend fun getMessages(channel: String, lastKnownId: Int, fetchMessagesCount: Int): List<Model>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<Model>)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT DISTINCT channel FROM messages")
    suspend fun getChannels(): List<String>
}