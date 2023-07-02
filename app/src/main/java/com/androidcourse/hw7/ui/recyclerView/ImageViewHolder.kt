package com.androidcourse.hw7.ui.recyclerView

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.androidcourse.hw7.R

class ImageViewHolder(root: View) : MessageViewHolder(root) {
    private val progressBar = root.findViewById<ProgressBar>(R.id.progress_circular)

    fun bindData(newData: Bitmap) {
        (data as ImageView).setImageBitmap(newData)
    }

    fun bind(message: ImageMessageWithBitmap) {
        metadataBind(message.metadata)
        bindData(message.data)
    }

    fun progress() {
        (data as ImageView).visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    fun image() {
        progressBar.visibility = View.GONE
        (data as ImageView).visibility = View.VISIBLE
    }
}