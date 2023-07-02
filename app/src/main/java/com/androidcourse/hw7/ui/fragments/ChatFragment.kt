package com.androidcourse.hw7.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidcourse.hw7.databinding.FragmentChatBinding
import com.androidcourse.hw7.ui.activities.OpenImageActivity
import com.androidcourse.hw7.ui.activities.TakePhotoActivity
import com.androidcourse.hw7.ui.recyclerView.*
import com.androidcourse.hw7.utils.MyApp
import com.androidcourse.hw7.viewModels.ChatViewModel
import kotlinx.coroutines.*

class ChatFragment : Fragment() {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var binding: FragmentChatBinding
    private lateinit var recyclerAdapter: AdapterWithDelegates<Message>

    private var mLastTouchY: Float = 0.0F
    private var scrollType = 0
    private var scrollDirection = 0
    private var scrollJob: Job? = null
    private val scrollScope = CoroutineScope(Dispatchers.Default)

    private val selectPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { selectedImage ->
            if (selectedImage != null) {
                chatViewModel.onImagePicked(selectedImage)
            }
        }
    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                @Suppress("DEPRECATION")
                chatViewModel.onImagePicked(result.data!!.getParcelableExtra("result")!!)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        setChannelNameLiveDataObserver()
        setMessagesLiveDataObserver()
        initRecycler()
        chatViewModel.fetchNewMessages()
        setScrollButtonListener()
        setGetImageButtonListener()
        setSendMessageButtonListener()
        setSyncButtonListener()
    }

    fun changeChannel(newChannel: String) {
        chatViewModel.changeChannel(newChannel)
    }

    private fun myOnTouchEvent(rv: RecyclerView, event: MotionEvent?) {
        scrollDirection = 0
        scrollType = 0
        rv.stopScroll()
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                val dy: Float = event.y - mLastTouchY
                if (dy > 0) {
                    binding.scrollToDownButton.visibility = View.GONE
                    binding.scrollToUpButton.visibility = View.VISIBLE
                } else if (dy < 0) {
                    binding.scrollToUpButton.visibility = View.GONE
                    binding.scrollToDownButton.visibility = View.VISIBLE
                }
                mLastTouchY = event.y
            }
            MotionEvent.ACTION_DOWN -> {
                mLastTouchY = event.y
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                scrollJob?.cancel()
                scrollJob = scrollScope.launch {
                    delay(5000)
                    withContext(Dispatchers.Main) {
                        binding.scrollToUpButton.visibility = View.GONE
                        binding.scrollToDownButton.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setChannelNameLiveDataObserver() {
        chatViewModel.channelLiveData.observe(viewLifecycleOwner) {
            binding.channelName.text = it
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSyncButtonListener() {
        binding.syncButton.setOnClickListener {
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    private fun setSendMessageButtonListener() {
        binding.sendMessageButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            binding.messageEditText.text.clear()
            chatViewModel.sendMessage(message)
        }
    }

    private fun showImageSourceDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose source")
            .setItems(arrayOf<CharSequence>("Gallery", "Camera")) { _, which ->
                when (which) {
                    0 -> {
                        selectPhotoLauncher.launch("image/*")
                    }
                    1 -> {
                        if (MyApp.photoPermissions.all {
                                ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    it
                                ) == PackageManager.PERMISSION_GRANTED
                            }
                        ) {
                            takePhotoLauncher.launch(Intent(context, TakePhotoActivity::class.java))
                        } else {
                            MyApp.instance.makeToast("You didn't allow camera use")
                        }
                    }
                }
            }
        builder.show()
    }

    private fun setGetImageButtonListener() {
        binding.getImageButton.setOnClickListener {
            showImageSourceDialog()
//            selectPhotoLauncher.launch("image/*")
        }
    }

    private fun setScrollButtonListener() {
        binding.scrollToDownButton.setOnClickListener {
            if (scrollDirection == 1) {
                scrollType = 2
                binding.RecyclerView.scrollToPosition(Integer.max(0, recyclerAdapter.itemCount - 1))
            } else {
                scrollDirection = 1
                scrollType = 1
                binding.RecyclerView.smoothScrollToPosition(
                    Integer.max(
                        0,
                        recyclerAdapter.itemCount - 1
                    )
                )
            }
            chatViewModel.fetchAll()
        }
        binding.scrollToUpButton.setOnClickListener {
            if (scrollDirection == 0 && scrollType == 1) {
                binding.RecyclerView.scrollToPosition(0)
            } else {
                scrollDirection = 0
                scrollType = 1
                binding.RecyclerView.smoothScrollToPosition(0)
            }
        }
    }

    private fun setMessagesLiveDataObserver() {
        chatViewModel.liveData.observe(viewLifecycleOwner) {
            recyclerAdapter.update(it)
            if (scrollDirection == 1) {
                if (scrollType == 1) {
                    binding.RecyclerView.smoothScrollToPosition(
                        Integer.max(
                            0,
                            recyclerAdapter.itemCount - 1
                        )
                    )
                } else {
                    binding.RecyclerView.scrollToPosition(
                        Integer.max(
                            0,
                            recyclerAdapter.itemCount - 1
                        )
                    )
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initRecycler() {
        val delegates = listOf(
            TextMessageDelegate {},
            ImageMessageDelegate(this@ChatFragment) {
                val intent =
                    Intent(context, OpenImageActivity::class.java)
                intent.putExtra("link", it.data as String)
                startActivity(intent)
            }
        )
        recyclerAdapter = AdapterWithDelegates(
            delegates
        )
        binding.RecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        scrollDirection = 1
                        scrollType = 1
                        chatViewModel.fetchNewMessages()
                    }
                }
            })
            setOnTouchListener { rv, ev ->
                myOnTouchEvent(rv as RecyclerView, ev)
                false
            }
        }
    }

    override fun onDestroy() {
        scrollScope.cancel()
        super.onDestroy()
    }
}