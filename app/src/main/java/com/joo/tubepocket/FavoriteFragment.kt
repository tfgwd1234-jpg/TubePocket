package com.joo.tubepocket

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// 기존의 보관함 디자인(fragment_storage)을 100% 똑같이 재활용합니다!
class FavoriteFragment : Fragment(R.layout.fragment_storage) {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter: VideoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // 보관함과 동일하게 리스트를 찾아서 어댑터를 연결합니다.
        val rvStorage = view.findViewById<RecyclerView>(R.id.rvStorage)
        rvStorage.layoutManager = LinearLayoutManager(context)

        adapter = VideoAdapter(emptyList())
        rvStorage.adapter = adapter

        // [핵심] 공용 창고에서 영상들을 가져오되, 하트(isFavorite)가 켜진(true) 영상만 쏙쏙 걸러냅니다!
        sharedViewModel.videoList.observe(viewLifecycleOwner) { videos ->
            val favoriteVideos = videos.filter { it.isFavorite == true }
            adapter.updateData(favoriteVideos)
        }
    }
}