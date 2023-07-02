package com.androidcourse.hw7.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidcourse.hw7.ui.recyclerView.Channel
import com.androidcourse.hw7.utils.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatsViewModel : ViewModel() {
    val channelsLiveData: MutableLiveData<List<Channel>> =
        MutableLiveData()
    private val coroutineScopeMain = CoroutineScope(Dispatchers.Main)
    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private var getJob: Job? = null

    fun getChannels(requireFromServer: Boolean = false) {
        coroutineScopeMain.launch {
            getJob?.join()
            getJob = viewModelScope.launch {
                channelsLiveData.postValue(
                    MyApp.instance.messagesRepository.getChannels(
                        requireFromServer
                    ).map {
                        Channel(it)
                    })
            }
        }
    }
}