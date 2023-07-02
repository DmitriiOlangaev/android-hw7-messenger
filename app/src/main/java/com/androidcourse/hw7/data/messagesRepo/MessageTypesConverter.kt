package com.androidcourse.hw7.data.messagesRepo

import androidx.room.TypeConverter

class MessageTypesConverter {
    @TypeConverter
    fun fromString(value: String): MessagesTypes {
        return enumValueOf(value)
    }

    @TypeConverter
    fun fromMessagesTypes(type: MessagesTypes): String {
        return type.name
    }
}
