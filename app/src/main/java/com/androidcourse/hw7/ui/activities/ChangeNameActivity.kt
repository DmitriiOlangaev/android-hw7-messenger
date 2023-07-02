package com.androidcourse.hw7.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androidcourse.hw7.databinding.ActivityChangeNameBinding

class ChangeNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeNameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.sendButton.setOnClickListener {
            if (binding.changeNameEditText.text.toString().isEmpty()) {
                return@setOnClickListener
            }
            val returnIntent = Intent()
            returnIntent.putExtra("result", binding.changeNameEditText.text.toString())
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}