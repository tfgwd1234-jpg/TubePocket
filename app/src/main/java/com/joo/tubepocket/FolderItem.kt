package com.joo.tubepocket

data class FolderItem(
    val name: String = "",
    var parentName: String = "",
    var orderIndex: Int = 0, // [추가] 드래그한 순서를 기억하는 번호표
    var depth: Int = 0       // [추가] 1단계, 2단계, 3단계... 깊이를 기억하는 곳
)