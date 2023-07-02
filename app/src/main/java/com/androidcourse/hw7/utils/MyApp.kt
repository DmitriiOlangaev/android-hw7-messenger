package com.androidcourse.hw7.utils

import android.Manifest
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.androidcourse.hw7.data.ImagesRepository
import com.androidcourse.hw7.data.messagesRepo.MessagesRepository
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.text.SimpleDateFormat
import java.util.*

class MyApp : Application() {

    companion object {
        lateinit var instance: MyApp
            private set
        val photoPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    lateinit var messagesRepository: MessagesRepository
        private set
    lateinit var imagesRepository: ImagesRepository
        private set
    val sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy-HH:mm:ss", Locale("ru"))
    private lateinit var uiHandler: Handler

    fun makeToast(message: String) {
        uiHandler.post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun isOnline(): Boolean {
        return try {
            val timeoutMs = 1500
            val sock = Socket()
            val sockAddress: SocketAddress = InetSocketAddress("8.8.8.8", 53)
            sock.connect(sockAddress, timeoutMs)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        uiHandler = Handler(Looper.getMainLooper())
        messagesRepository = MessagesRepository()
        imagesRepository = ImagesRepository()
    }

    override fun onTerminate() {
        imagesRepository.close()
        messagesRepository.close()
        super.onTerminate()
    }
}