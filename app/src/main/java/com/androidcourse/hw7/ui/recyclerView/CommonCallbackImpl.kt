package com.androidcourse.hw7.ui.recyclerView

import androidx.recyclerview.widget.DiffUtil

class CommonCallbackImpl<T>(
    private val oldItems: List<T>,
    private val newItems: List<T>,
    val areItemsTheSameImpl: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem },
    val areContentsTheSameImpl: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem }
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return areItemsTheSameImpl(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return areContentsTheSameImpl(oldItem, newItem)
    }
}