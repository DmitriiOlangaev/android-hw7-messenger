package com.androidcourse.hw7.ui.recyclerView

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidcourse.hw7.R


open class MessageViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    private val sender = root.findViewById<TextView>(R.id.senderName)
    private val date = root.findViewById<TextView>(R.id.dateOfMessage)
    private val messageId = root.findViewById<TextView>(R.id.messageId)
    val data: Any = root.findViewById(R.id.dataOfMessage)

    fun metadataBind(metadata: MessageMetadata) {
        sender.text = metadata.senderName
        date.text = metadata.date
        messageId.text = metadata.id.toString()
    }

}
