package com.androidcourse.hw7.data.messagesRepo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Model(
    @PrimaryKey
    val id: Int,
    val from: String,
    val channel: String,
    val type: MessagesTypes,
    val data: String,
    val time: Long,
)
