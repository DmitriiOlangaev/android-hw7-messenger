package com.androidcourse.hw7.ui.recyclerView

data class ImageMessageWithLink(
    override val metadata: MessageMetadata,
    override val data: String
) : Message