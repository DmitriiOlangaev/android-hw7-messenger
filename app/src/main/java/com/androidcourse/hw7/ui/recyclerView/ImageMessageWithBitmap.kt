package com.androidcourse.hw7.ui.recyclerView

import android.graphics.Bitmap

data class ImageMessageWithBitmap(
    override val metadata: MessageMetadata,
    override val data: Bitmap
) : MessageForViewHolders