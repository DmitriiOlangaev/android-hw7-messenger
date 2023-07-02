package com.androidcourse.hw7.ui.recyclerView

data class TextMessage(
    override val metadata: MessageMetadata,
    override val data: String
) : Message, MessageForViewHolders