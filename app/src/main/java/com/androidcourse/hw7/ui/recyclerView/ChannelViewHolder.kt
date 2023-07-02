package com.androidcourse.hw7.ui.recyclerView

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidcourse.hw7.R

class ChannelViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    val channelTextView = root.findViewById<TextView>(R.id.channelName)

    fun bind(channel: Channel) {
        channelTextView.text = channel.channelName
    }
}