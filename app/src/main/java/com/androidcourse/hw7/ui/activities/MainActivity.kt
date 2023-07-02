package com.androidcourse.hw7.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.androidcourse.hw7.R
import com.androidcourse.hw7.databinding.ActivityMainBinding
import com.androidcourse.hw7.ui.fragments.ChatFragment
import com.androidcourse.hw7.ui.fragments.ChatsFragment
import com.androidcourse.hw7.utils.MyApp
import com.androidcourse.hw7.viewModels.MainViewModel


class MainActivity : AppCompatActivity(), ChatsFragment.ChatsFragmentListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var chatFragment: ChatFragment
    private lateinit var chatsFragment: ChatsFragment

    companion object {
        private const val START_REQUEST_PERMISSION_CODE = 0
    }

    private val changeNameLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                mainViewModel.onNameChange(result.data?.getStringExtra("result").toString())
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.change_name -> changeNameLauncher.launch(
                Intent(
                    this@MainActivity,
                    ChangeNameActivity::class.java
                )
            )
            R.id.clear_all -> mainViewModel.clearAll()
            R.id.clear_thumb -> mainViewModel.clearThumb()
            R.id.clear_img -> mainViewModel.clearImg()
            R.id.clear_messages_db -> mainViewModel.clearMessagesDb()
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        chatFragment.dispatchTouchEvent(ev)
//        return super.dispatchTouchEvent(ev)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        adjustFragments()
        requestPermissions(MyApp.photoPermissions, START_REQUEST_PERMISSION_CODE)
    }

    private fun adjustFragments() {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                chatsFragment = ChatsFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainActivityFragmentContainer, chatsFragment).commit()
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                chatFragment = ChatFragment()
                chatsFragment = ChatsFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.landLeftFragmentContainer, chatsFragment).commit()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.landRightFragmentContainer, chatFragment).commit()
            }
            else -> {
                MyApp.instance.makeToast("Unusual phone orientation")
            }
        }
    }

    override fun onChatClicked(chatName: String) {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                val transaction = supportFragmentManager.beginTransaction()
                chatFragment = ChatFragment()
                transaction.replace(R.id.mainActivityFragmentContainer, chatFragment)
                transaction.addToBackStack(null)
                transaction.commit()
                supportFragmentManager.executePendingTransactions()
                chatFragment.changeChannel(chatName)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                chatFragment.changeChannel(chatName)
            }
            else -> {
                MyApp.instance.makeToast("Unusual phone orientation")
            }
        }
    }

}