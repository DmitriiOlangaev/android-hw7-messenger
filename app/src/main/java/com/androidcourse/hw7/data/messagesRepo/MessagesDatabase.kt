package com.androidcourse.hw7.data.messagesRepo

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Model::class], version = 1)
@TypeConverters(MessageTypesConverter::class)
abstract class MessagesDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}

