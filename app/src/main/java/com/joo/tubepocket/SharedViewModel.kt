package com.joo.tubepocket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SharedViewModel : ViewModel() {

    private val _videoList = MutableLiveData<MutableList<VideoItem>>(mutableListOf())
    // 1. 폴더 리스트를 보관하는 변수
    private val _folderList = MutableLiveData<MutableList<FolderItem>>(mutableListOf())
    val folderList: LiveData<MutableList<FolderItem>> get() = _folderList

    // 2. 파이어베이스에서 폴더 목록을 가져오는 함수 (init 블록 안에 fetchFoldersFromFirebase() 도 호출하게 해주세요)
    fun fetchFoldersFromFirebase() {
        db?.collection("folders")
            ?.addSnapshotListener { snapshot, e ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = mutableListOf<FolderItem>()
                    for (doc in snapshot) {
                        val item = doc.toObject(FolderItem::class.java)
                        list.add(item)
                    }
                    _folderList.value = list
                }
            }
    }

    // 3. 새 폴더를 파이어베이스에 저장하는 함수
    fun addFolder(folder: FolderItem) {
        // 폴더 이름을 파이어베이스 문서 ID로 사용해서 중복을 막아요!
        db?.collection("folders")?.document(folder.name)?.set(folder)
    }
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

            // 👇 이 한 줄을 반드시 추가해야 앱이 켜질 때 폴더를 가져와서 화면에 보여줍니다! 👇
            fetchFoldersFromFirebase()

        } catch (e: Exception) {
            Log.e("SharedViewModel", "Firebase 초기화 오류", e)
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
    // 데이터 수정 함수
    fun updateVideo(oldVideo: VideoItem, newVideo: VideoItem) {
        db?.collection("videos")
            ?.whereEqualTo("timestamp", oldVideo.timestamp) // 같은 시간값인 데이터를 찾아요
            ?.get()
            ?.addOnSuccessListener { documents ->
                for (document in documents) {
                    // 찾은 문서의 내용을 newVideo 정보로 싹 바꿔줍니다
                    db?.collection("videos")?.document(document.id)?.set(newVideo)
                }
            }
    }
    // SharedViewModel.kt 내부에 추가
    fun deleteVideo(item: VideoItem) {
        // 1. Firebase에서 삭제
        db?.collection("videos")
            ?.whereEqualTo("videoUrl", item.videoUrl) // URL을 기준으로 해당 데이터 찾기
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    db?.collection("videos")?.document(document.id)?.delete()
                }
            }
    }
    // 👈 [새로 추가] 태그 이름을 바꾸는 마법의 함수
    fun updateTagName(oldTag: String, newTag: String) {
        val currentList = _videoList.value ?: return

        // 모든 영상을 돌면서 기존 태그 글자를 새 태그 글자로 변경합니다.
        val updatedList = currentList.map { video ->
            if (video.tags.contains(oldTag)) {
                // 영상의 전체 태그 문자열에서 옛날 태그를 새 태그로 쏙 바꿉니다.
                // 예: "#게임 #요리" 에서 "#게임"을 "#스포츠"로 변환
                val newTagsString = video.tags.replace(oldTag, newTag)
                video.copy(tags = newTagsString) // 태그가 바뀐 새 영상 데이터로 교체
            } else {
                video // 해당 태그가 없는 영상은 그대로 둡니다.
            }
        }

        // 전광판(_videoList)을 업데이트하여 화면이 스스로 다시 그려지게 합니다.
        _videoList.value = updatedList.toMutableList() // 👈 변경된 부분!
    }

    // 👈 [새로 추가] 태그를 삭제하는 마법의 함수
    fun deleteTag(tagToDelete: String) {
        val currentList = _videoList.value ?: return

        // 모든 영상을 돌면서 해당 태그를 지워버립니다.
        val updatedList = currentList.map { video ->
            if (video.tags.contains(tagToDelete)) {
                // 태그를 빈칸으로 지우고, 앞뒤 공백을 깔끔하게 정리합니다.
                var newTagsString = video.tags.replace(tagToDelete, "").trim()
                // 연속된 공백이 생겼다면 하나로 줄여줍니다.
                newTagsString = newTagsString.replace("\\s+".toRegex(), " ")
                video.copy(tags = newTagsString)
            } else {
                video
            }
        }

        _videoList.value = updatedList.toMutableList() // 👈 변경된 부분!
    }
}