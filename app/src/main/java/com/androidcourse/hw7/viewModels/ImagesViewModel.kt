package com.androidcourse.hw7.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidcourse.hw7.utils.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class ImagesViewModel :
    ViewModel() {

    private var lastKey: String = ""
    private var lastLiveData: MutableLiveData<Bitmap?> = getDefualtLiveData()
    private var lastStillNeed = AtomicBoolean(true)
    private val imageRepository = MyApp.instance.imagesRepository
    private val viewModelScope = CoroutineScope(Default)


    private fun getDefualtLiveData() = MutableLiveData<Bitmap?>(null)

    fun getData(key: String, type: String): Pair<LiveData<Bitmap?>, AtomicBoolean> {
        lastKey = "$type/$key"
        lastLiveData = getDefualtLiveData()
        lastStillNeed = AtomicBoolean(true)
        return Pair(lastLiveData, lastStillNeed)
    }

    fun onDataReceived() {
        val key = lastKey
        val liveData = lastLiveData
        val stillNeed = lastStillNeed
        viewModelScope.launch(IO) {
            if (!stillNeed.get()) {
                return@launch
            }
            val pic = imageRepository.get(key)
            liveData.postValue(pic)
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}