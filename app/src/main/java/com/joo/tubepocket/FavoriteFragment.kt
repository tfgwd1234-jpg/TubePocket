package com.joo.tubepocket

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoriteFragment : Fragment(R.layout.fragment_storage) {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter: VideoAdapter

    // --- 새로 추가되는 부분 시작 ---
    private var currentSortOption = 0

    private fun getSortedList(list: List<VideoItem>): List<VideoItem> {
        return when (currentSortOption) {
            0 -> list.sortedByDescending { it.timestamp } // 최신순
            1 -> list.sortedBy { it.timestamp }           // 오래된순
            2 -> list.sortedBy { it.title }               // 이름순
            else -> list
        }
    }
    // --- 새로 추가되는 부분 끝 ---

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        // 👉 [요청 1번 반영] 휴대폰 물리 뒤로가기 버튼 누르면 홈으로 이동!
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()
                // 하단 메뉴 불빛도 홈으로 켜주기
                (activity as? MainActivity)?.updateMenuUI("HOME")
            }
        })

        // 👉 [요청 3번 반영] 즐겨찾기 화면에서는 '폴더보기' 글자 안 보이게 숨기기 (디자인은 그대로!)
        val btnViewFolder = view.findViewById<TextView>(R.id.btnViewFolder)
        btnViewFolder.visibility = View.GONE

        // --- 새로 추가되는 부분 시작: 스피너 설정 및 동작 ---
        val spinnerSort = view.findViewById<android.widget.Spinner>(R.id.spinnerSort)
        val sortOptions = arrayOf("등록일자(최신순)", "등록일자(오래된순)", "이름순")
        // [요청 반영] 어댑터를 만들 때 글자색과 여백을 직접 수정하도록 설정합니다.
        val spinnerAdapter = object : android.widget.ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, sortOptions) {
            // 1. 현재 선택된 뷰 (기존과 동일)
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.BLACK)
                view.setPadding(0, 0, 0, 0)
                return view
            }

            // 2. [추가] 클릭해서 펼쳐진 목록 뷰 (여기를 수정하면 목록 간격이 넓어집니다!)
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.WHITE)
                // 위아래 간격을 40 정도로 주면 목록이 시원하게 떨어집니다.
                view.setPadding(30, 40, 30, 40)
                return view
            }
        }
        spinnerSort.adapter = spinnerAdapter

        spinnerSort.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSortOption = position

                // 수정된 부분: view 대신 requireView()를 사용합니다.
                val etSearch = requireView().findViewById<EditText>(R.id.etSearch)
                val query = etSearch.text.toString()

                val allVideos = sharedViewModel.videoList.value ?: mutableListOf()
                val favoriteVideos = allVideos.filter { it.isFavorite == true }

                val finalList = if (query.isEmpty()) {
                    favoriteVideos
                } else {
                    favoriteVideos.filter { item ->
                        item.title.contains(query, ignoreCase = true) ||
                                item.tags.contains(query, ignoreCase = true)
                    }
                }
                adapter.updateData(getSortedList(finalList))
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        // --- 새로 추가되는 부분 끝 ---

        // 화면 제목을 '모든 영상'에서 '즐겨찾기'로 변경 (선택 센스!)
        view.findViewById<TextView>(R.id.tvCurrentFolder)?.text = "즐겨찾기"

        // 리사이클러뷰(영상 목록) 세팅
        val rvStorage = view.findViewById<RecyclerView>(R.id.rvStorage)
        rvStorage.layoutManager = LinearLayoutManager(context)
        adapter = VideoAdapter(emptyList())
        rvStorage.adapter = adapter

        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        // 영상을 공용 창고에서 가져오기
        sharedViewModel.videoList.observe(viewLifecycleOwner) { videos ->
            val favoriteVideos = videos.filter { it.isFavorite == true }

            // 검색창이 비어있으면 그냥 즐겨찾기 전체를 보여줌
            if (etSearch.text.isEmpty()) {
                adapter.updateData(getSortedList(favoriteVideos))
            }
        }

        // 👉 [요청 4번 반영] 즐겨찾기 안에서 한 글자만 입력해도 실시간 검색!
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                // 전체 영상 중에 즐겨찾기인 것만 먼저 빼옴
                val allVideos = sharedViewModel.videoList.value ?: mutableListOf()
                val favoriteVideos = allVideos.filter { it.isFavorite == true }

                if (query.isEmpty()) {
                    adapter.updateData(getSortedList(favoriteVideos))
                } else {
                    // 제목이나 태그에 검색어가 있는 것만 걸러내기
                    val filteredList = favoriteVideos.filter { item ->
                        item.title.contains(query, ignoreCase = true) ||
                                item.tags.contains(query, ignoreCase = true)
                    }
                    adapter.updateData(getSortedList(filteredList))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}