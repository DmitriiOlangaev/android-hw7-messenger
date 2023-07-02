package com.androidcourse.hw7.ui.recyclerView

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class AdapterWithDelegates<T>(
    private val delegates: List<Delegate<T>>,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data: List<T> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun update(updateValue: Pair<DiffUtil.DiffResult?, List<T>>) {
        data = updateValue.second
        if (updateValue.first != null) {
            updateValue.first!!.dispatchUpdatesTo(this)
        } else {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        delegates[viewType].getViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegates[getItemViewType(position)].bindViewHolder(
            holder,
            data[position]
        )
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int =
        delegates.indexOfFirst { delegate -> delegate.forItem(data[position]) }

}