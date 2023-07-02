package com.androidcourse.hw7.network

import android.graphics.BitmapFactory
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit

object Server {
    private val retrofit = Retrofit.Builder().baseUrl("http://213.189.221.170:8008").build()


    private val moshi = Moshi.Builder().build()
    private val adapterForListOfJsonMessage = moshi.adapter<List<JsonMessage>>(
        Types.newParameterizedType(
            List::class.java,
            JsonMessage::class.java
        )
    ).lenient()
    private val adapterForChannels = moshi.adapter<List<String>>(
        Types.newParameterizedType(
            List::class.java,
            String::class.java
        )
    )
    private val adapterForJsonMessage =
        moshi.adapter<JsonMessage>(JsonMessage::class.java).lenient()

    private val service = retrofit.create(ApiService::class.java)
    private const val link: String = "http://213.189.221.170:8008"

    @JsonClass(generateAdapter = true)
    data class JsonMessage(
        @Json(name = "id") val id: Int?,
        @Json(name = "from") val from: String,
        @Json(name = "to") val to: String,
        @Json(name = "data") val data: JsonMessageData,
        @Json(name = "time") val time: Long?
    )

    @JsonClass(generateAdapter = true)
    data class JsonMessageData(
        @Json(name = "Text") val text: JsonMessageText?,
        @Json(name = "Image") val image: JsonMessageImage?
    )

    @JsonClass(generateAdapter = true)
    data class JsonMessageText(
        @Json(name = "text") val text: String
    )

    @JsonClass(generateAdapter = true)
    data class JsonMessageImage(
        @Json(name = "link") val link: String
    )

    data class Response(val data: Any?, val responseCode: Int, val responseMessage: String)

    private fun getDefaultResponse(): Response =
        Response(null, 504, "Gateway timeout")


    private fun getJsonStringFromResponse(response: retrofit2.Response<ResponseBody>): String? {
        return response.body()?.charStream().use { it?.readText() }
    }

    suspend fun getChannels(): Response {
        val response = service.getChannels()
        val jsonString = getJsonStringFromResponse(service.getChannels())
        val data = coroutineScope {
            async(IO) {
                if (jsonString != null) {
                    adapterForChannels.fromJson(jsonString)
                } else {
                    listOf()
                }
            }
        }.await()
        return Response(data, response.code(), response.message())
    }

    suspend fun downloadImageByLink(path: String): Response {
        val response = service.downloadImage(path)
        if (response.isSuccessful && response.body() != null) {
            return Response(
                BitmapFactory.decodeStream(response.body()!!.byteStream()),
                response.code(),
                response.message()
            )
        }
        return getDefaultResponse()
    }

    suspend fun getMessages(channel: String, lastKnownId: Int, count: Int): Response {
        val response = service.getMessages(channel, lastKnownId, count)
        val jsonString = response.body()?.charStream().use { it?.readText() }
        val data = coroutineScope {
            async(IO) {
                if (jsonString != null) {
                    adapterForListOfJsonMessage.fromJson(jsonString)
                } else {
                    null
                }
            }
        }.await()
        return Response(data, response.code(), response.message())
    }

    suspend fun sendTextMessage(channel: String, from: String, message: String): Response {
        val messageJson = adapterForJsonMessage.toJson(
            JsonMessage(
                null, from, channel, JsonMessageData(
                    JsonMessageText(message), null
                ), null
            )
        )
        val response =
            service.postData(RequestBody.create(MediaType.parse("application/json"), messageJson))
        return Response(null, response.code(), response.message())
    }

    suspend fun postBitmap(
        channel: String,
        from: String,
        fileName: String,
        fileType: String,
        data: ByteArray
    ): Response {
        val messageJson = adapterForJsonMessage.toJson(
            JsonMessage(
                null,
                from,
                channel,
                JsonMessageData(null, JsonMessageImage(fileName)),
                null
            )
        )
        val imageRequestBody = RequestBody.create(MediaType.parse("image/$fileType"), data)
        val jsonRequestBody = RequestBody.create(MediaType.parse("application/json"), messageJson)
        val jsonPart = MultipartBody.Part.createFormData("msg", "", jsonRequestBody)
        val imagePart = MultipartBody.Part.createFormData("pic", fileName, imageRequestBody)
        val response = service.uploadImage(jsonPart, imagePart)
        return Response(null, response.code(), response.message())
    }
}