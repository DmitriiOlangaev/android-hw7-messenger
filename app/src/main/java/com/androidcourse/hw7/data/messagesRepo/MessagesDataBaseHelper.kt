package com.androidcourse.hw7.data.messagesRepo

import android.content.Context
import androidx.room.Room

class MessagesDataBaseHelper(context: Context) {

    private val database: MessagesDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            MessagesDatabase::class.java,
            "messages_database.db"
        ).build()
    }

    fun messageDao() = database.messageDao()
}