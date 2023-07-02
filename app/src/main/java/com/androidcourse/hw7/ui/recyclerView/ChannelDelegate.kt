package com.androidcourse.hw7.ui.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.androidcourse.hw7.R

class ChannelDelegate(override val onItemClick: (Chat) -> Unit) : Delegate<Chat> {
    override fun forItem(listItem: Chat): Boolean = listItem is Channel

    override fun getViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ChannelViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_channel, parent, false)
    )

    override fun bindViewHolder(viewHolder: RecyclerView.ViewHolder, item: Chat) {
        (viewHolder as ChannelViewHolder).bind(item as Channel)
        viewHolder.channelTextView.setOnClickListener {
            onItemClick(item)
        }
    }

}
