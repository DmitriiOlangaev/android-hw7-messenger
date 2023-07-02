package com.androidcourse.hw7.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidcourse.hw7.databinding.FragmentChatsBinding
import com.androidcourse.hw7.ui.recyclerView.AdapterWithDelegates
import com.androidcourse.hw7.ui.recyclerView.Channel
import com.androidcourse.hw7.ui.recyclerView.ChannelDelegate
import com.androidcourse.hw7.ui.recyclerView.Chat
import com.androidcourse.hw7.viewModels.ChatsViewModel

class ChatsFragment : Fragment() {

    interface ChatsFragmentListener {
        fun onChatClicked(chatName: String)
    }

//    companion object {
//        @Volatile
//        private var instance: ChatsFragment? = null
//
//        fun getInstance(): ChatsFragment {
//            return instance ?: synchronized(this) {
//                instance ?: ChatsFragment().also { instance = it }
//            }
//        }
//    }

    private lateinit var chatsViewModel: ChatsViewModel
    private lateinit var binding: FragmentChatsBinding
    private lateinit var recyclerAdapter: AdapterWithDelegates<Chat>
    private lateinit var callback: ChatsFragmentListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as ChatsFragmentListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatsViewModel = ViewModelProvider(this)[ChatsViewModel::class.java]
        setChannelsLiveDataObserver()
        initRecycler()
        chatsViewModel.getChannels(true)
    }

    private fun setChannelsLiveDataObserver() {
        chatsViewModel.channelsLiveData.observe(viewLifecycleOwner) {
            recyclerAdapter.update(Pair(null, it))
        }
    }

    private fun initRecycler() {
        recyclerAdapter = AdapterWithDelegates(listOf(ChannelDelegate {
            callback.onChatClicked((it as Channel).channelName)
        }))
        binding.chatsListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerAdapter
        }
    }


}