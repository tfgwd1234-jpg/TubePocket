package com.joo.tubepocket

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)

        // UI 요소 연결
        val ivClose = findViewById<ImageView>(R.id.ivClose)
        val ivShare = findViewById<ImageView>(R.id.ivShare)
        val ivMore = findViewById<ImageView>(R.id.ivMore)
        val ivDetailThumbnail = findViewById<ImageView>(R.id.ivDetailThumbnail)
        val btnPlayVideo = findViewById<CardView>(R.id.btnPlayVideo)
        val tvDetailDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvDetailTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvDetailTags = findViewById<TextView>(R.id.tvDetailTags)
        val tvDetailMemo = findViewById<TextView>(R.id.tvDetailMemo)

        // 어댑터에서 넘겨준 데이터 받기
        val title = intent.getStringExtra("title") ?: ""
        val tags = intent.getStringExtra("tags") ?: ""
        val memoText = intent.getStringExtra("memo") ?: ""
        val thumbnailUrl = intent.getStringExtra("thumbnailUrl") ?: ""
        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        val timestamp = intent.getLongExtra("timestamp", 0L)

        // 메모 앞의 "Memo: " 텍스트 제거 후 깔끔하게 표시
        val cleanMemo = memoText.replace("Memo: ", "")

        // 날짜 포맷 변환 (예: 2024-03-20 저장)
        val sdf = SimpleDateFormat("yyyy-MM-dd 저장", Locale.KOREA)
        val dateString = if (timestamp > 0) sdf.format(Date(timestamp)) else "저장 날짜 없음"

        // 화면에 데이터 세팅
        tvDetailTitle.text = title
        tvDetailTags.text = tags
        tvDetailMemo.text = cleanMemo
        tvDetailDate.text = dateString

        if (thumbnailUrl.isNotEmpty()) {
            Glide.with(this).load(thumbnailUrl).into(ivDetailThumbnail)
        }

        // 1. 닫기 버튼 클릭
        ivClose.setOnClickListener {
            finish()
        }

        // 2. 영상 재생 버튼 클릭 (유튜브 앱 또는 브라우저 실행)
        btnPlayVideo.setOnClickListener {
            if (videoUrl.isNotEmpty()) {
                val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                startActivity(playIntent)
            } else {
                Toast.makeText(this, "재생할 링크가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. 공유하기 버튼 클릭 (안드로이드 기본 공유 창 띄우기)
        ivShare.setOnClickListener {
            if (videoUrl.isNotEmpty()) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "$title\n$videoUrl")
                }
                startActivity(Intent.createChooser(shareIntent, "영상 링크 공유하기"))
            } else {
                Toast.makeText(this, "공유할 링크가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. 점 세개(더보기) 버튼 클릭 - 수정/삭제 팝업 메뉴
        ivMore.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add(0, 0, 0, "수정")
            popup.menu.add(0, 1, 0, "삭제")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    0 -> {
                        Toast.makeText(this, "수정 화면으로 이동 (준비 중)", Toast.LENGTH_SHORT).show()
                        true
                    }
                    1 -> {
                        Toast.makeText(this, "삭제 확인 팝업 (준비 중)", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}