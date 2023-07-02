package com.androidcourse.hw7.ui.recyclerView

sealed interface AbstractMessage {
    val metadata: MessageMetadata
    val data: Any
}