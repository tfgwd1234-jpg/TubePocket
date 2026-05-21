package com.joo.tubepocket

data class VideoItem(
    val title: String = "",
    val tags: String = "",
    val memo: String = "",
    val duration: String = "",
    val isShorts: Boolean = false,
    val thumbnailUrl: String = "",
    val timestamp: Long = 0L,
    val videoUrl: String = "" // 영상 재생 및 공유를 위한 원본 링크 추가
)