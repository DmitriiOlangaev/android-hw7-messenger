package com.androidcourse.hw7.ui.recyclerView

import android.view.View
import android.widget.TextView


class TextViewHolder(root: View) : MessageViewHolder(root) {

    fun bindData(newData: String) {
        (data as TextView).text = newData
    }

    fun bind(message: TextMessage) {
        metadataBind(message.metadata)
        bindData(message.data)
    }
}
