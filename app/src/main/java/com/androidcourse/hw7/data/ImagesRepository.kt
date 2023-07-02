package com.androidcourse.hw7.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.androidcourse.hw7.R
import com.androidcourse.hw7.network.Server
import com.androidcourse.hw7.utils.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ImagesRepository : AutoCloseable {
    private val dir = File(MyApp.instance.cacheDir, "Images")
    private val coroutineScope = CoroutineScope(IO)
    private val busyFiles: MutableMap<String, AtomicBoolean> =
        ConcurrentHashMap<String, AtomicBoolean>()
    private val dirStatus = AtomicInteger(0)
    private val bitmapPlaceholder =
        BitmapFactory.decodeResource(MyApp.instance.resources, R.drawable.image_placeholder)

    init {
        if (!dir.exists()) {
            dir.mkdir()
        }
        Log.d("ImagesRepository", "Cache dir is ${dir.absolutePath}")
    }

    fun deleteFilesPredicate(predicate: (String) -> Boolean) {
        coroutineScope.launch {
            Log.d("ImageRepository", "start of deleting files")
            while (dirStatus.compareAndSet(0, -1)) {
                //Nothing
            }
            val filesList = dir.listFiles() ?: return@launch
            for (file in filesList) {
                doWork<Unit>(
                    file.name.toString(),
                ) {
                    if (predicate(Uri.decode(it))) {
                        file.delete()
                    }
                }
            }
            Log.d("ImageRepository", "all files was deleted")
            dirStatus.set(0)
        }
    }

    suspend fun get(key: String): Bitmap {
        var res: Bitmap? = getFromStorage(key)
        if (res == null && MyApp.instance.isOnline()) {
            res = getFromNetwork(key)
            if (res != null) {
                writeToStorage(key, res)
            }
        }

        return res ?: bitmapPlaceholder
    }

    private suspend fun getFromNetwork(key: String): Bitmap? {
        val response = Server.downloadImageByLink(key)
        Log.i(
            this::class.java.name,
            "$key was got from network," +
                    " result code = ${response.responseCode}," +
                    " response message = ${response.responseMessage}"
        )
        return response.data as Bitmap?
    }

    private fun <T> doWork(
        key: String,
        callable: (String) -> T,
    ): T {
        busyFiles.computeIfAbsent(key) { _ -> AtomicBoolean(false) }
        while (true) {
            val ok = busyFiles[key]!!.compareAndSet(false, true)
            if (!ok) {
                continue
            }
            val res = callable(key)
            busyFiles[key]!!.set(false)
            return res
        }
    }

    private fun getFromStorage(key: String): Bitmap? {
        Log.d("ImagesRepository", "Reading from cache with key = $key")
        return doWork<Bitmap?>(Uri.encode(key)) {
            val file = File(dir, it)
            if (file.exists()) {
                Log.i(this::class.java.name, "$key was got from cache")
                val fis = FileInputStream(file)
                fis.use {
                    val result = BitmapFactory.decodeStream(fis)
                    fis.close()
                    result
                }

            } else {
                null
            }
        }
    }

    private fun writeToStorage(key: String, pic: Bitmap) {
        Log.d("ImagesRepository", "Writing to cache with key = $key")
        coroutineScope.launch {
            while (
                dirStatus.updateAndGet {
                    if (it >= 0) {
                        it + 1
                    } else {
                        it
                    }
                } == -1) {
                //Nothing
            }
            doWork<Unit>(Uri.encode(key)) {
                val file = File(dir, it)
                if (file.exists()) {
                    file.delete()
                }
                file.createNewFile()
                file.outputStream().use { fos ->
                    pic.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }
                Log.d("ImagesRepository", "$key was written to cache")
            }
            dirStatus.decrementAndGet()
        }
    }

    override fun close() {
        coroutineScope.cancel()
    }

}
