package com.androidcourse.hw7.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("messages")
    suspend fun postData(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @GET
    suspend fun downloadImage(@Url imageUrl: String): Response<ResponseBody>

    @Multipart
    @POST("messages")
    suspend fun uploadImage(
        @Part jsonPart: MultipartBody.Part,
        @Part imagePart: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("channels")
    suspend fun getChannels(): Response<ResponseBody>

    @GET("channel/{channel}")
    suspend fun getMessages(
        @Path("channel") channel: String,
        @Query("lastKnownId") lastKnownId: Int,
        @Query("limit") limit: Int,
        @Query("reverse") reverse: Boolean = false
    ): Response<ResponseBody>
}