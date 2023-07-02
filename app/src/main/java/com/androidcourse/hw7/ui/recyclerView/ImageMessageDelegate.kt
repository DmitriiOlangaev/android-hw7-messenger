package com.androidcourse.hw7.ui.recyclerView

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.androidcourse.hw7.R
import com.androidcourse.hw7.utils.DebugTags
import com.androidcourse.hw7.viewModels.ImagesViewModel
import java.util.concurrent.atomic.AtomicBoolean

class ImageMessageDelegate(
    private val fragment: Fragment,
    override val onItemClick: (Message) -> Unit
) : Delegate<Message> {

    private val imagesViewModel = ViewModelProvider(
        fragment
    )[ImagesViewModel::class.java]
    private val map = HashMap<ImageViewHolder, Pair<LiveData<Bitmap?>, AtomicBoolean>>()

    override fun forItem(listItem: Message): Boolean =
        listItem is ImageMessageWithLink

    override fun getViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        ImageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_image, parent, false)
        )

    override fun bindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        item: Message
    ) {
        if (viewHolder !is ImageViewHolder) {
            Log.d(
                DebugTags.UNEXPECTED_SHIT.toString(),
                "In ImageMessageDelegate.bindViewHolder got not ImageViewHolder"
            )
            throw IllegalStateException()
        }
        if (item !is ImageMessageWithLink) {
            Log.d(
                DebugTags.UNEXPECTED_SHIT.toString(),
                "In ImageMessageDelegate.bingViewHolder got not ImageMessageWithLink"
            )
            throw IllegalStateException()
        }
        viewHolder.metadataBind(item.metadata)
        (viewHolder.data as ImageView).setOnClickListener {
            onItemClick(item)
        }
        val tempData = map[viewHolder]
        if (tempData != null) {
            tempData.first.removeObservers(fragment)
            tempData.second.set(false)
        }
        viewHolder.progress()
        val data = imagesViewModel.getData(item.data, "thumb")
        map[viewHolder] = data
        data.first.observe(fragment) {
            if (it != null) {
                viewHolder.bindData(it)
                viewHolder.image()
                assert(map[viewHolder] == data)
                if (map[viewHolder] == data) {
                    map.remove(viewHolder)
                }
            }
        }
        imagesViewModel.onDataReceived()
    }
}