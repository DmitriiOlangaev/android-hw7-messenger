package com.androidcourse.hw7.data.messagesRepo

import com.androidcourse.hw7.network.Server
import com.androidcourse.hw7.utils.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MessagesRepository : AutoCloseable {

    private val messageDao = MessagesDataBaseHelper(MyApp.instance).messageDao()
    private val messagesRepoCoroutineScope = CoroutineScope(IO)

    @Suppress("UNCHECKED_CAST")
    suspend fun getChannels(requireFromServer: Boolean): List<String> {
        return if (requireFromServer && MyApp.instance.isOnline()) {
            Server.getChannels().data as List<String>
        } else {
            messageDao.getChannels()
        }
    }


    suspend fun getMessages(
        channel: String,
        lastKnownId: Int,
        fetchMessagesCount: Int
    ): MutableList<Model> {
        val res: MutableList<Model> =
            messageDao.getMessages(channel, lastKnownId, fetchMessagesCount).toMutableList()
//        if (res.isNotEmpty() && kotlin.math.abs(res.first().id - lastKnownId) > 100) {
//            res.clear()
//        }
        if (res.size < fetchMessagesCount && MyApp.instance.isOnline()) {
            res.apply {
                addAll(
                    getMessagesFromNetwork(
                        channel,
                        if (res.isEmpty()) lastKnownId else res.last().id,
                        fetchMessagesCount - res.size
                    )
                )
            }
        }
        return res
    }

    private suspend fun getMessagesFromNetwork(
        channel: String,
        lastKnownId: Int,
        fetchMessagesCount: Int
    ): List<Model> {
        if (!MyApp.instance.isOnline()) {
            return mutableListOf()
        }
        val response = Server.getMessages(channel, lastKnownId, fetchMessagesCount)
        if (response.responseCode != 200) {
            return mutableListOf()
        }
        val messages = (response.data as List<*>).filterIsInstance<Server.JsonMessage>()

        val models = messages.map {
            Model(
                it.id!!,
                it.from,
                it.to,
                if (it.data.image == null) MessagesTypes.TEXT else MessagesTypes.IMAGE,
                (it.data.image?.link ?: it.data.text?.text)!!, it.time!!
            )
        }

        messagesRepoCoroutineScope.launch {
            saveMessages(models)
        }
        return models
    }

    private suspend fun saveMessages(messages: List<Model>) {
        messageDao.insertAllMessages(messages)
    }

    fun clearMessagesDb() {
        messagesRepoCoroutineScope.launch {
            messageDao.deleteAllMessages()
        }
    }

    override fun close() {
        messagesRepoCoroutineScope.cancel()
    }
}

