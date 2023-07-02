package com.androidcourse.hw7.ui.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidcourse.hw7.R

class TextMessageDelegate(override val onItemClick: (Message) -> Unit) : Delegate<Message> {

    override fun forItem(listItem: Message): Boolean = listItem is TextMessage

    override fun getViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        TextViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_text, parent, false)
        )

    override fun bindViewHolder(viewHolder: RecyclerView.ViewHolder, item: Message) {
        (viewHolder as TextViewHolder).bind(item as TextMessage)
        (viewHolder.data as TextView).setOnClickListener {
            onItemClick(item)
        }
    }

}