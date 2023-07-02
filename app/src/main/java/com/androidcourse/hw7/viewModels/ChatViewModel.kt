package com.androidcourse.hw7.viewModels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import com.androidcourse.hw7.data.SharedPreferencesHelper
import com.androidcourse.hw7.data.messagesRepo.MessagesTypes
import com.androidcourse.hw7.data.messagesRepo.Model
import com.androidcourse.hw7.network.Server
import com.androidcourse.hw7.ui.recyclerView.*
import com.androidcourse.hw7.utils.MyApp
import kotlinx.coroutines.*

class ChatViewModel : ViewModel() {
    val liveData: MutableLiveData<Pair<DiffUtil.DiffResult?, List<Message>>> =
        MutableLiveData()
    val channelLiveData: MutableLiveData<String> = MutableLiveData("1@channel")
    private val fetchMessagesCount = 30

    @Volatile
    private var messages: MutableList<Message> = ArrayList()
    private val coroutineScopeMain = CoroutineScope(Dispatchers.Main)
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val defaultName = "Ivan Ivanov"

    private var sendJob: Job? = null
    private var getJob: Job? = null
    private val sharedPreferencesHelper = SharedPreferencesHelper.getInstance(MyApp.instance)
    private val startID = 0

    private fun getName() = sharedPreferencesHelper.get("name", defaultName)

    private fun getLastKnownId(): Int {
        return if (messages.isEmpty()) {
            startID
        } else {
            messages.last().metadata.id
        }
    }

    private fun createMessageMetadata(model: Model): MessageMetadata =
        MessageMetadata(
            model.from,
            model.id,
            MyApp.instance.sdf.format(model.time).toString()
        )

    private fun updateMessages(newMessages: List<Message>) {
        val newList: MutableList<Message> = messages.toMutableList().apply {
            addAll(newMessages)
        }
        val diffResult = DiffUtil.calculateDiff(
            CommonCallbackImpl<Message>(
                messages.toList(),
                newList.toList(),
            )
        )
        messages = newList
        liveData.postValue(Pair(diffResult, newList))
    }

    private suspend fun getNewMessages(): List<Message> {
        return MyApp.instance.messagesRepository.getMessages(
            channelLiveData.value!!,
            getLastKnownId(),
            fetchMessagesCount
        ).map {
            when (it.type) {
                MessagesTypes.TEXT -> TextMessage(
                    createMessageMetadata(it),
                    it.data
                )
                MessagesTypes.IMAGE -> ImageMessageWithLink(
                    createMessageMetadata(it),
                    it.data
                )
            }

        }
    }

    fun changeChannel(newChannel: String) {
        messages.clear()
        channelLiveData.value = newChannel
        liveData.value = Pair(null, messages)
        coroutineScopeMain.launch {
            getJob?.cancelAndJoin()
            fetchNewMessages()
        }
    }


    fun fetchNewMessages() {
        coroutineScopeMain.launch {
            getJob?.join()
            getJob = viewModelScope.launch {
                val newMessages = withContext(Dispatchers.IO) {
                    getNewMessages()
                }
                updateMessages(newMessages)
            }
        }
    }

    fun fetchAll() {
        coroutineScopeMain.launch {
            getJob?.join()
            getJob = viewModelScope.launch {
                while (isActive) {
                    val newMessages = withContext(Dispatchers.IO) {
                        getNewMessages()
                    }
                    if (newMessages.isEmpty()) {
                        break
                    }
                    updateMessages(newMessages)
                }
            }
        }
    }

    fun onImagePicked(uri: Uri?) {
        if (uri == null) return
        val curName = getName()
        val curChannel = channelLiveData.value!!

        coroutineScopeMain.launch A@{
            sendJob?.join()
            sendJob = viewModelScope.launch(Dispatchers.IO) B@{
                val fileType =
                    MyApp.instance.contentResolver.getType(uri)?.removePrefix("image/") ?: return@B
                val fileName = System.currentTimeMillis().toString() + ".$fileType"
                val imageStream = MyApp.instance.contentResolver.openInputStream(uri) ?: return@B
                imageStream.use {
                    val byteArray = it.readBytes()
                    val response = Server.postBitmap(
                        channel = curChannel,
                        from = curName,
                        fileName = fileName,
                        fileType = fileType,
                        data = byteArray
                    )
                    MyApp.instance.makeToast(response.responseCode.toString() + " " + response.responseMessage)
                }
            }
        }
    }

    fun sendMessage(message: String) {
        if (message.isEmpty()) {
            return
        }
        val curName = getName()
        val curChannel = channelLiveData.value!!
        coroutineScopeMain.launch {
            sendJob?.join()
            sendJob = viewModelScope.launch(Dispatchers.IO) {
                val response = Server.sendTextMessage(curChannel, curName, message)
                MyApp.instance.makeToast(response.responseCode.toString() + " " + response.responseMessage)
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        coroutineScopeMain.cancel()
        super.onCleared()
    }
}