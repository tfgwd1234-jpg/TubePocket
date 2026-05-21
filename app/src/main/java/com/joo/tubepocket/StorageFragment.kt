package com.joo.tubepocket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.OnBackPressedCallback

class StorageFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter: VideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 메인 액티비티에 종속된 뷰모델(공용 창고) 가져오기
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val rvStorage = view.findViewById<RecyclerView>(R.id.rvStorage)
        rvStorage.layoutManager = LinearLayoutManager(context)

        // 초기에는 빈 리스트로 어댑터 연결
        adapter = VideoAdapter(emptyList())
        rvStorage.adapter = adapter

        // 공용 창고의 리스트 변경을 감지(observe)하여 자동으로 어댑터 갱신
        sharedViewModel.videoList.observe(viewLifecycleOwner) { currentList ->
            adapter.updateData(currentList)
        }
        // UI 요소 연결
        val ivBack = view.findViewById<ImageView>(R.id.ivBack)
        val ivSearch = view.findViewById<ImageView>(R.id.ivSearch)
        val ivSetting = view.findViewById<ImageView>(R.id.ivSetting)
        val btnViewFolder = view.findViewById<TextView>(R.id.btnViewFolder)

        // 좌측 상단 뒤로가기 버튼
        ivBack.setOnClickListener {
            navigateToHome()
        }

        // 폴더보기 버튼 클릭 시
        btnViewFolder.setOnClickListener {
            Toast.makeText(context, "폴더보기 화면으로 이동할게요!", Toast.LENGTH_SHORT).show()
            // 다음에 구현할 코드 자리입니다.
        }

        // onViewCreated 안에 아래 코드들을 넣으세요
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        // 1. 뒤로가기 기능 (좌측 상단 버튼)
        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        // 물리 뒤로가기 버튼
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })

        // 3. 한 글자만 입력해도 실시간 검색
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val originalList = sharedViewModel.videoList.value ?: mutableListOf()

                if (query.isEmpty()) {
                    adapter.updateData(originalList)
                } else {
                    // [핵심] 제목(title) 또는 태그(tags)에 해당 글자가 포함되어 있는지 검사
                    val filteredList = originalList.filter { item ->
                        item.title.contains(query, ignoreCase = true) ||
                                item.tags.contains(query, ignoreCase = true)
                    }
                    adapter.updateData(filteredList)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    // 홈으로 이동하는 공통 함수
    private fun navigateToHome() {
        // 1. 프래그먼트 교체
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()

        // 2. MainActivity의 메뉴 UI 업데이트 호출
        (activity as? MainActivity)?.updateMenuUI(isHomeActive = true)
    }
}