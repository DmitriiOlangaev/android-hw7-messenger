package com.androidcourse.hw7.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.androidcourse.hw7.databinding.ActivityOpenImageBinding
import com.androidcourse.hw7.viewModels.ImagesViewModel

class OpenImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpenImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageViewModel =
            ViewModelProvider(this)[ImagesViewModel::class.java]
        binding = ActivityOpenImageBinding.inflate(layoutInflater)
        val intent = this.intent
        val link = intent.getStringExtra("link")
        binding.imageLink.text = link
        setContentView(binding.root)
        val liveData = imageViewModel.getData("$link", "img")
        liveData.first.observe(this@OpenImageActivity) {
            if (it != null) {
                binding.img.setImageBitmap(it)
                binding.progressCircular.visibility = View.GONE
                binding.img.visibility = View.VISIBLE
            }
        }
        imageViewModel.onDataReceived()
    }

}