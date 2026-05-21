package com.joo.tubepocket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SharedViewModel : ViewModel() {

    private val _videoList = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    val videoList: LiveData<MutableList<VideoItem>> get() = _videoList

    // db 변수를 nullable(?)로 변경하여 에러 발생 시 앱이 꺼지지 않도록 1차 방어합니다.
    private var db: FirebaseFirestore? = null

    init {
        // 초기 샘플 데이터 세팅 (에러 방어 및 오프라인 테스트 용도)
        val initialData = mutableListOf(
            VideoItem("세상에서 가장 쉬운 파스타 만들기 (10분 완성)", "#파스타 #간단요리 #자취생", "Memo: 면수는 무조건 남겨...", "10:24", false, "https://img.youtube.com/vi/1yVuCR36Zxg/mqdefault.jpg"),
            VideoItem("스쿼트 정석 자세 알려드림 #shorts", "#운동 #스쿼트 #웨이트", "Memo: 무릎이 발끝을 넘지...", "0:58", true, "https://img.youtube.com/vi/q6hBSSfokzY/mqdefault.jpg"),
            VideoItem("React 19 비기너 가이드 - Server Components ...", "#React #개발 #프론트엔드", "Memo: Use hook과 actions...", "15:45", false, "https://img.youtube.com/vi/aZkjO02A1xI/mqdefault.jpg")
        )
        _videoList.value = initialData

        // Firebase 초기화 에러를 잡아내어 앱 강제 종료(Crash)를 100% 방지하는 핵심 try-catch 로직입니다.
        try {
            db = FirebaseFirestore.getInstance()
            fetchVideosFromFirebase()
        } catch (e: Exception) {
            Log.e("SharedViewModel", "Firebase 초기화 오류: google-services 플러그인 또는 json 파일 확인 필요", e)
        }
    }

    private fun fetchVideosFromFirebase() {
        db?.collection("videos")
            ?.orderBy("timestamp", Query.Direction.DESCENDING)
            ?.addSnapshotListener { snapshot, e ->
                // 통신 오류 발생 시 안전하게 처리
                if (e != null) {
                    Log.w("SharedViewModel", "데이터 불러오기 실패", e)
                    return@addSnapshotListener
                }

                // 데이터베이스에 변화가 생기면 자동으로 리스트를 갱신
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = mutableListOf<VideoItem>()
                    for (doc in snapshot) {
                        val item = doc.toObject(VideoItem::class.java)
                        list.add(item)
                    }
                    _videoList.value = list
                }
            }
    }

    // 모달에서 영상을 저장할 때 호출되는 함수
    fun addVideo(item: VideoItem) {
        // db가 정상적으로 연결되어 있으면 서버에 저장, 아니면 앱 내 임시 리스트에만 추가하여 에러를 회피합니다.
        if (db != null) {
            db?.collection("videos")?.add(item)
                ?.addOnFailureListener { e ->
                    Log.w("SharedViewModel", "데이터 저장 실패", e)
                }
        } else {
            val currentList = _videoList.value ?: mutableListOf()
            currentList.add(0, item)
            _videoList.value = ArrayList(currentList)
        }
    }
}