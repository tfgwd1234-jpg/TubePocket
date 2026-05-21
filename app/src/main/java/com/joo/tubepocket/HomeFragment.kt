package com.joo.tubepocket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter: VideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뷰모델 가져오기
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // 2. 리사이클러뷰 세팅
        val rvRecentVideos = view.findViewById<RecyclerView>(R.id.rvRecentVideos)
        rvRecentVideos.layoutManager = LinearLayoutManager(context)

        adapter = VideoAdapter(emptyList())
        rvRecentVideos.adapter = adapter

        // 3. 최근 영상 5개만 가져오기
        sharedViewModel.videoList.observe(viewLifecycleOwner) { list ->
            val recentList = list.take(5) // 리스트의 상위 5개만 가져옴
            adapter.updateData(recentList)
        }

        // 4. '전체보기' 클릭 시 보관함으로 이동
        val tvViewAll = view.findViewById<TextView>(R.id.tvViewAll) // XML에 id 추가 필요
        tvViewAll.setOnClickListener {
            // MainActivity의 하단 메뉴 '보관함'을 클릭하는 것과 같은 효과
            (activity as? MainActivity)?.findViewById<android.view.View>(R.id.tvMenuStorage)?.performClick()
        }
    }
}