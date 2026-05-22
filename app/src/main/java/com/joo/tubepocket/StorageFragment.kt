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
        val ivSearch = view.findViewById<ImageView>(R.id.ivSearch)
        val ivSetting = view.findViewById<ImageView>(R.id.ivSetting)
        val btnViewFolder = view.findViewById<TextView>(R.id.btnViewFolder)


        // 폴더보기 버튼 클릭 시
        btnViewFolder.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FolderFragment())
                .commit()
        }

        // onViewCreated 안에 아래 코드들을 넣으세요
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

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
        // onViewCreated 안쪽 윗부분에 추가하세요.
        val currentFolder = arguments?.getString("FOLDER_NAME") ?: "모든 영상"

        // 2. 휴대폰 물리 뒤로가기 버튼 클릭 시
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleStorageBackPress(currentFolder)
            }
        })
        val tvCurrentFolder = view.findViewById<TextView>(R.id.tvCurrentFolder)

        // xml을 수정하지 않고 코드에서 글자를 바꿔줍니다!
        tvCurrentFolder?.text = currentFolder

        // 기존의 sharedViewModel.videoList.observe 부분을 아래처럼 바꿔주세요.
        sharedViewModel.videoList.observe(viewLifecycleOwner) { currentList ->
            val filteredList = if (currentFolder == "모든 영상") {
                currentList
            } else {
                // 태그에 #폴더이름 이 포함된 영상만 쏙쏙 골라냅니다!
                currentList.filter { it.tags.contains("#$currentFolder") }
            }
            adapter.updateData(filteredList)
        }
    }
    // [새로 추가하는 똑똑한 뒤로가기 함수] - onViewCreated 밖에 적어주세요!
    private fun handleStorageBackPress(currentFolder: String) {
        // 내가 태그에서 넘어왔는지 확인하는 임시 표시표를 꺼내봅니다. 기본값은 false입니다.
        val isFromTag = arguments?.getBoolean("IS_FROM_TAG", false) == true

        if (isFromTag) {
            // 👈 만약 태그 화면에서 온 게 맞다면, 뒤로가기 시 태그 관리 화면으로 돌려보냅니다!
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TagFragment())
                .commit()
            (activity as? MainActivity)?.updateMenuUI("TAG") // 하단 불빛도 태그로 켭니다.
        } else if (currentFolder == "모든 영상") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
            (activity as? MainActivity)?.updateMenuUI("HOME")
        } else {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FolderFragment())
                .commit()
        }
    }
    // 홈으로 이동하는 공통 함수
    private fun navigateToHome() {
        // 1. 프래그먼트 교체
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()

        // 2. MainActivity의 메뉴 UI 업데이트 호출
        (activity as? MainActivity)?.updateMenuUI("HOME") // 👈 "HOME" 문자열로 변경!
    }
    companion object {
        // 폴더 이름을 담아서 StorageFragment를 만드는 마법의 상자
        fun newInstance(folderName: String): StorageFragment {
            val fragment = StorageFragment()
            val args = Bundle()
            args.putString("FOLDER_NAME", folderName)
            args.putBoolean("IS_FROM_TAG", false) // 👈 폴더에서 올 때는 false라고 적어둡니다.
            fragment.arguments = args
            return fragment
        }

        // 👈 [새로 추가] 태그 이름을 담아서 StorageFragment를 만드는 마법의 상자
        fun newTagInstance(tagName: String): StorageFragment {
            val fragment = StorageFragment()
            val args = Bundle()
            args.putString("FOLDER_NAME", tagName)
            args.putBoolean("IS_FROM_TAG", true) // 👈 태그에서 올 때는 true라고 표시를 남깁니다!
            fragment.arguments = args
            return fragment
        }
    }
}