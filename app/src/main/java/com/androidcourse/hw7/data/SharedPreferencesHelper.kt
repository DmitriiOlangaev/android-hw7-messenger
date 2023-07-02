package com.androidcourse.hw7.data

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(private val sharedPreferences: SharedPreferences) {

    fun put(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun get(key: String, default: String): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    companion object {
        @Volatile
        private var instance: SharedPreferencesHelper? = null

        fun getInstance(context: Context): SharedPreferencesHelper =
            instance ?: synchronized(this) {
                instance ?: SharedPreferencesHelper(
                    context.getSharedPreferences(
                        "MySharedPreferences",
                        Context.MODE_PRIVATE
                    )
                ).also { instance = it }
            }
    }
}