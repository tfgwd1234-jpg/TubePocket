package com.joo.tubepocket

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TagFragment : Fragment(R.layout.fragment_tag) {

    private lateinit var rvTagList: RecyclerView
    private lateinit var adapter: TagAdapter
    private lateinit var sharedViewModel: SharedViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        rvTagList = view.findViewById(R.id.rvTagList)
        rvTagList.layoutManager = LinearLayoutManager(context)

        // 어댑터 연결 및 클릭 이벤트 설정
        adapter = TagAdapter(emptyList(), emptyList(),
            onItemClick = { clickedTagName ->
                val cleanTagName = clickedTagName.replace("#", "").trim()
                val mainActivity = requireActivity() as MainActivity
                mainActivity.updateMenuUI("STORAGE")

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, StorageFragment.newTagInstance(cleanTagName)) // 👈 newTagInstance로 변경!
                    .commit()
            },
            onItemLongClick = { clickedTagName ->
                // [해결 2] 태그를 길게 누르면 커스텀 팝업창을 띄웁니다.
                showCustomTagDialog(clickedTagName)
            }
        )
        rvTagList.adapter = adapter

        // 영상 목록에서 태그만 쏙쏙 뽑아오기 (띄어쓰기 기준 조각내기 반영)
        sharedViewModel.videoList.observe(viewLifecycleOwner) { videos ->
            val uniqueTags = mutableSetOf<String>()

            videos.forEach { video ->
                val tagWords = video.tags.split(" ")
                tagWords.forEach { word ->
                    val cleanWord = word.trim()
                    if (cleanWord.startsWith("#")) {
                        uniqueTags.add(cleanWord)
                    }
                }
            }

            val tagList = uniqueTags.sorted()
            adapter.updateData(tagList, videos)
        }

        // 휴대폰 물리 뒤로가기 버튼 처리: 홈 화면으로 이동
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val mainActivity = requireActivity() as MainActivity
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()
                mainActivity.updateMenuUI("HOME")
            }
        })
    }

    // [핵심 기능] 커스텀 디자인 팝업창을 만들고 띄우는 함수입니다.
    // [핵심 기능] 커스텀 디자인 팝업창을 만들고 띄우는 함수입니다.
    private fun showCustomTagDialog(oldTagName: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_tag, null)

        val etTagName = dialogView.findViewById<EditText>(R.id.etTagName)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<TextView>(R.id.btnDelete)
        val btnEdit = dialogView.findViewById<TextView>(R.id.btnEdit)

        // 👈 [수정 1] 이제 화면에 #이 고정되어 있으니, 입력창에는 #을 뺀 글자만 쏙 넣어줍니다.
        val cleanOldName = oldTagName.replace("#", "")
        etTagName.setText(cleanOldName)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        val alertDialog = builder.create()

        // [취소 버튼 클릭]
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        // 👈 [수정 2] 삭제하기 전에 "진짜 삭제할까요?" 물어보는 확인창 띄우기
        btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("태그 삭제")
                .setMessage("'$oldTagName' 태그를 삭제하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    sharedViewModel.deleteTag(oldTagName)
                    Toast.makeText(requireContext(), "'$oldTagName' 태그가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss() // 기존 팝업창도 같이 닫아줍니다.
                }
                .setNegativeButton("아니오", null) // 아니오 누르면 아무 일도 안 일어남
                .show()
        }

        // 👈 [수정 3] 수정 버튼 누를 때 다시 '#'을 자동으로 붙여서 저장하기
        btnEdit.setOnClickListener {
            val inputText = etTagName.text.toString().trim()
            val newTagName = "#$inputText" // 무조건 앞에 #을 다시 예쁘게 붙여줍니다!

            if (inputText.isNotEmpty() && newTagName != oldTagName) {
                sharedViewModel.updateTagName(oldTagName, newTagName)
                Toast.makeText(requireContext(), "'$oldTagName'가 '$newTagName'(으)로 수정되었습니다.", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "변경할 태그 이름을 올바르게 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.show()
    }
} // 👈 이 중괄호가 TagFragment 클래스를 닫아주는 마지막 중괄호입니다!