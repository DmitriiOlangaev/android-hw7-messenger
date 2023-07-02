package com.androidcourse.hw7.viewModels

import androidx.lifecycle.ViewModel
import com.androidcourse.hw7.data.SharedPreferencesHelper
import com.androidcourse.hw7.utils.MyApp

class MainViewModel : ViewModel() {
    private val sharedPreferencesHelper = SharedPreferencesHelper.getInstance(MyApp.instance)
    fun clearAll() {
        clearThumb()
        clearImg()
        clearMessagesDb()
    }

    fun clearThumb() {
        MyApp.instance.imagesRepository.deleteFilesPredicate { it.startsWith("thumb") }
    }

    fun clearImg() {
        MyApp.instance.imagesRepository.deleteFilesPredicate { it.startsWith("img") }
    }

    fun clearMessagesDb() {
        MyApp.instance.messagesRepository.clearMessagesDb()
    }

    fun onNameChange(newName: String) {
        sharedPreferencesHelper.put("name", newName)
    }

}