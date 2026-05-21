package com.joo.tubepocket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_link, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        val ivClose = view.findViewById<ImageView>(R.id.ivClose)
        val etYoutubeLink = view.findViewById<EditText>(R.id.etYoutubeLink)
        val chipGroupFolder = view.findViewById<ChipGroup>(R.id.chipGroupFolder)
        val etTags = view.findViewById<EditText>(R.id.etTags)
        val etMemo = view.findViewById<EditText>(R.id.etMemo)
        val btnSaveLink = view.findViewById<Button>(R.id.btnSaveLink)

        ivClose.setOnClickListener {
            dismiss()
        }

        btnSaveLink.setOnClickListener {
            val link = etYoutubeLink.text.toString().trim()
            val tags = etTags.text.toString().trim()
            val memo = etMemo.text.toString().trim()

            val selectedChipId = chipGroupFolder.checkedChipId
            var folderName = "일반"
            if (selectedChipId != View.NO_ID) {
                val selectedChip = view.findViewById<Chip>(selectedChipId)
                folderName = selectedChip.text.toString()
            }

            if (link.isEmpty()) {
                Toast.makeText(context, "유튜브 링크를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSaveLink.isEnabled = false
            btnSaveLink.text = "영상 정보 불러오는 중..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val doc = Jsoup.connect(link).get()
                    val fetchedTitle = doc.select("meta[property=og:title]").attr("content")
                    val fetchedThumb = doc.select("meta[property=og:image]").attr("content")

                    withContext(Dispatchers.Main) {
                        val newVideo = VideoItem(
                            title = if (fetchedTitle.isNotEmpty()) fetchedTitle else "제목을 불러올 수 없습니다",
                            tags = if (tags.isNotEmpty()) tags else "#$folderName",
                            memo = if (memo.isNotEmpty()) "Memo: $memo" else "Memo: 없음",
                            duration = "0:00",
                            isShorts = link.contains("shorts", ignoreCase = true),
                            thumbnailUrl = fetchedThumb,
                            timestamp = System.currentTimeMillis(), // Firestore 정렬을 위한 현재 시간 추가
                            videoUrl = link // 추가된 부분: 원본 유튜브 링크 저장
                        )

                        sharedViewModel.addVideo(newVideo)
                        Toast.makeText(context, "[$folderName] 보관함에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnSaveLink.isEnabled = true
                        btnSaveLink.text = "보관함에 저장하기"
                        Toast.makeText(context, "유튜브 링크 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}