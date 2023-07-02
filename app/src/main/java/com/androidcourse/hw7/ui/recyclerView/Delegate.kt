package com.androidcourse.hw7.ui.recyclerView

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface Delegate<T> {

    val onItemClick: (T) -> Unit
    fun forItem(listItem: T): Boolean
    fun getViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    fun bindViewHolder(viewHolder: RecyclerView.ViewHolder, item: T)
}