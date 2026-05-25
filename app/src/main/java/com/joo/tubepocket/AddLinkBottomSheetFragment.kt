package com.joo.tubepocket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class AddLinkBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private var editingVideo: VideoItem? = null

    fun setEditingData(video: VideoItem) {
        this.editingVideo = video
    }

    // 👇 여기부터 추가하세요 👇
    private var sharedLink: String? = null

    fun setSharedLink(link: String) {
        this.sharedLink = link
    }
    // 👆 여기까지 추가하세요 👆

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_link, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 👇 여기부터 추가하세요 👇
        // 화면 하단 내비게이션바와 겹치지 않도록 아래쪽에 여백(패딩)을 추가합니다.
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom + 60)
            insets
        }
        // 👆 여기까지 추가하세요 👆

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val ivClose = view.findViewById<ImageView>(R.id.ivClose)
        val etYoutubeLink = view.findViewById<EditText>(R.id.etYoutubeLink)
        // ⭕ 추가: 새로 만든 UI 요소들 연결 및 선택된 폴더 저장 변수
        val tvSelectedFolderInfo = view.findViewById<TextView>(R.id.tvSelectedFolderInfo)
        val rvFolderTree = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvFolderTree)
        var selectedFolderName = "기본" // 제일 처음에는 '기본' 폴더로 설정해 둡니다.

        val etTags = view.findViewById<android.widget.MultiAutoCompleteTextView>(R.id.etTags)
        val etMemo = view.findViewById<EditText>(R.id.etMemo)
        val btnSaveLink = view.findViewById<Button>(R.id.btnSaveLink)

        // 1. 보관함과 동일한 어댑터(트리구조)를 사용해서 폴더 목록 띄우기
        val folderAdapter = FolderAdapter(mutableListOf(), emptyList()) { clickedFolderName ->
            // 폴더를 클릭하면, 선택된 폴더 이름을 기억하고 화면 글씨도 예쁘게 바꿔줍니다!
            selectedFolderName = clickedFolderName
            tvSelectedFolderInfo.text = "선택된 폴더: $selectedFolderName"
        }
        rvFolderTree.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        rvFolderTree.adapter = folderAdapter

        sharedViewModel.folderList.observe(viewLifecycleOwner) { folders ->
            val videos = sharedViewModel.videoList.value ?: emptyList()

            // 👉 [요청 1번 반영] 컴퓨터에게 번호표(orderIndex) 순서대로 똑바로 줄을 서라고 명령합니다!
            val sortedFolders = folders.sortedBy { it.orderIndex }.toMutableList()

            // 기존 코드에서 folders.toMutableList() 부분을 sortedFolders로 바꿔줍니다.
            folderAdapter.updateData(sortedFolders, videos)

            // 영상 수정 중일 때, 원래 들어있던 폴더를 찾아서 미리 글씨를 바꿔둡니다.
            if (editingVideo != null) {
                for (folder in folders) {
                    if (editingVideo?.tags?.contains("#${folder.name}") == true) {
                        selectedFolderName = folder.name
                        tvSelectedFolderInfo.text = "선택된 폴더: $selectedFolderName"
                        break
                    }
                }
            }
        }

        // [새로 추가된 기능] 기존 태그들을 모아서 콤보박스(자동완성) 어댑터에 연결합니다.
        sharedViewModel.videoList.observe(viewLifecycleOwner) { videos ->
            val uniqueTags = mutableSetOf<String>()
            videos.forEach { video ->
                // 태그들을 쉼표나 띄어쓰기 기준으로 쪼개서 모읍니다.
                video.tags.split(" ", ",").forEach { word ->
                    val cleanWord = word.trim()
                    if (cleanWord.startsWith("#")) {
                        uniqueTags.add(cleanWord)
                    }
                }
            }
            // 콤보박스에 모은 태그 리스트를 장착합니다.
            val tagAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, uniqueTags.toList())
            etTags.setAdapter(tagAdapter)
            etTags.setTokenizer(android.widget.MultiAutoCompleteTextView.CommaTokenizer()) // 쉼표(,)를 치면 자동으로 다음 단어를 추천해 줍니다.
        }

        // 2. 수정 모드일 때 기존 데이터를 화면에 채워줍니다.
        editingVideo?.let { video ->
            tvTitle.text = "영상 수정"
            etYoutubeLink.setText(video.videoUrl)
            etTags.setText(video.tags)
            etMemo.setText(video.memo.replace("Memo: ", ""))
            btnSaveLink.text = "수정하기"
        }

        // 👇 여기부터 추가하세요 👇
        // 넘어온 링크가 있다면 유튜브 링크 칸에 자동으로 입력해 줍니다!
        sharedLink?.let { link ->
            etYoutubeLink.setText(link)
        }
        // 👆 여기까지 추가하세요 👆

        ivClose.setOnClickListener { dismiss() }

        // 3. 저장(또는 수정) 버튼을 눌렀을 때
        btnSaveLink.setOnClickListener {
            val link = etYoutubeLink.text.toString().trim()
            val memo = etMemo.text.toString().trim()

            // [핵심] 콤보박스 자동완성으로 들어간 쉼표(,)를 빈칸(띄어쓰기)으로 깔끔하게 바꿔서 저장합니다!
            var tags = etTags.text.toString().replace(",", " ").replace("  ", " ").trim()

            if (link.isEmpty()) {
                Toast.makeText(context, "유튜브 링크를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 리사이클러뷰에서 터치해서 변수에 저장해둔 폴더 이름을 바로 사용합니다!
            val folderName = selectedFolderName

            // 폴더 이름을 태그(tags)에 자동으로 추가해 줍니다. (중복 방지)
            if (!tags.contains("#$folderName")) {
                tags = "$tags #$folderName".trim()
            }

            btnSaveLink.isEnabled = false
            btnSaveLink.text = "처리 중..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val doc = Jsoup.connect(link).get()
                    val fetchedTitle = doc.select("meta[property=og:title]").attr("content")
                    val fetchedThumb = doc.select("meta[property=og:image]").attr("content")

                    withContext(Dispatchers.Main) {
                        val newVideo = VideoItem(
                            title = if (fetchedTitle.isNotEmpty()) fetchedTitle else "제목을 불러올 수 없습니다",
                            tags = tags,
                            memo = if (memo.isNotEmpty()) "Memo: $memo" else "Memo: 없음",
                            duration = "0:00",
                            isShorts = link.contains("shorts", ignoreCase = true),
                            thumbnailUrl = fetchedThumb,
                            timestamp = editingVideo?.timestamp ?: System.currentTimeMillis(),
                            videoUrl = link
                        )

                        if (editingVideo != null) {
                            sharedViewModel.updateVideo(editingVideo!!, newVideo)
                            Toast.makeText(context, "영상 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            sharedViewModel.addVideo(newVideo)
                            Toast.makeText(context, "[$folderName] 보관함에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                        dismiss()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnSaveLink.isEnabled = true
                        btnSaveLink.text = if (editingVideo != null) "수정하기" else "보관함에 저장하기"
                        Toast.makeText(context, "정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}