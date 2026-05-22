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
import androidx.lifecycle.ViewModelProvider

class VideoDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 시스템 바 영역 확보를 위한 테마 설정 확인
        window.decorView.fitsSystemWindows = true
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
        // VideoDetailActivity.kt의 ivMore 클릭 이벤트 부분 수정
        ivMore.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add(0, 0, 0, "수정")
            popup.menu.add(0, 1, 0, "삭제")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    0 -> { // 수정
                        val bottomSheet = AddLinkBottomSheetFragment()
                        // 현재 데이터 전달
                        val currentVideo = VideoItem(title, tags, memoText, "0:00", false, thumbnailUrl, timestamp, videoUrl)
                        bottomSheet.setEditingData(currentVideo)
                        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
                        true
                    }
                    1 -> { // 삭제 확인 다이얼로그
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("삭제")
                            .setMessage("영상을 삭제 하시겠습니까?")
                            .setPositiveButton("예") { _, _ ->
                                // [수정된 부분] SharedViewModel을 명확히 가져와서 함수 호출
                                val viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
                                val currentVideo = VideoItem(title, tags, memoText, "0:00", false, thumbnailUrl, timestamp, videoUrl)

                                viewModel.deleteVideo(currentVideo) // 이제 인식이 될 거예요

                                Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .setNegativeButton("아니오", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
        val ivFavorite = findViewById<ImageView>(R.id.ivFavorite)
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // 주의: intent.getStringExtra("videoUrl") 부분은 현재 상세화면으로 넘어온 영상의 URL이나 고유값을 받는 변수로 맞춰주세요!
        val currentVideoUrl = intent.getStringExtra("videoUrl") ?: ""
        var isCurrentlyFavorite = false

        // 1. 화면이 켜지면 파이어베이스에서 현재 즐겨찾기 상태를 확인해서 하트 모양을 결정합니다.
        db.collection("videos").whereEqualTo("videoUrl", currentVideoUrl)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    isCurrentlyFavorite = document.getBoolean("isFavorite") ?: false
                    if (isCurrentlyFavorite) {
                        ivFavorite.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        ivFavorite.setImageResource(R.drawable.ic_heart_empty)
                    }
                }
            }

        // 2. 하트를 클릭했을 때의 마법 기능!
        ivFavorite.setOnClickListener {
            isCurrentlyFavorite = !isCurrentlyFavorite // true -> false, false -> true 로 뒤집기

            // 모양 바꾸고 메시지(Toast) 띄우기
            if (isCurrentlyFavorite) {
                ivFavorite.setImageResource(R.drawable.ic_heart_filled)
                android.widget.Toast.makeText(this, "즐겨찾기에 등록되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                ivFavorite.setImageResource(R.drawable.ic_heart_empty)
                android.widget.Toast.makeText(this, "즐겨찾기에 해제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
            }

            // 파이어베이스에 바뀐 상태 진짜로 저장하기
            db.collection("videos").whereEqualTo("videoUrl", currentVideoUrl)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("videos").document(document.id).update("isFavorite", isCurrentlyFavorite)
                    }
                }
        }
    }
}