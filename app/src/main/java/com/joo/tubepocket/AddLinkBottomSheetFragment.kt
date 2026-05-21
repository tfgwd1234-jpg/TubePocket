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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_link, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val ivClose = view.findViewById<ImageView>(R.id.ivClose)
        val etYoutubeLink = view.findViewById<EditText>(R.id.etYoutubeLink)
        val chipGroupFolder = view.findViewById<ChipGroup>(R.id.chipGroupFolder)
        val etTags = view.findViewById<EditText>(R.id.etTags)
        val etMemo = view.findViewById<EditText>(R.id.etMemo)
        val btnSaveLink = view.findViewById<Button>(R.id.btnSaveLink)

        // 1. 파이어베이스에 저장된 폴더를 가져와서 화면에 동그란 칩(Chip)으로 보여줍니다.
        sharedViewModel.folderList.observe(viewLifecycleOwner) { folders ->
            chipGroupFolder.removeAllViews() // 처음에 있는 껍데기 샘플 지우기
            for (folder in folders) {
                val chip = Chip(requireContext())
                chip.text = folder.name
                chip.isCheckable = true

                // 영상 수정 중일 때, 원래 선택되어 있던 폴더를 찾아서 체크해 둡니다.
                if (editingVideo?.tags?.contains("#${folder.name}") == true) {
                    chip.isChecked = true
                }
                chipGroupFolder.addView(chip)
            }
        }

        // 2. 수정 모드일 때 기존 데이터를 화면에 채워줍니다.
        editingVideo?.let { video ->
            tvTitle.text = "영상 수정"
            etYoutubeLink.setText(video.videoUrl)
            etTags.setText(video.tags)
            etMemo.setText(video.memo.replace("Memo: ", ""))
            btnSaveLink.text = "수정하기"
        }

        ivClose.setOnClickListener { dismiss() }

        // 3. 저장(또는 수정) 버튼을 눌렀을 때
        btnSaveLink.setOnClickListener {
            val link = etYoutubeLink.text.toString().trim()
            var tags = etTags.text.toString().trim()
            val memo = etMemo.text.toString().trim()

            if (link.isEmpty()) {
                Toast.makeText(context, "유튜브 링크를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // [핵심] 어떤 폴더(칩)가 선택되었는지 확인합니다!
            val selectedChipId = chipGroupFolder.checkedChipId
            val folderName = if (selectedChipId != View.NO_ID) {
                view.findViewById<Chip>(selectedChipId).text.toString()
            } else {
                "기본" // 아무것도 선택하지 않으면 '기본' 폴더로 지정
            }

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