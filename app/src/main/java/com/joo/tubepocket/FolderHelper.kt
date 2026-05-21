package com.joo.tubepocket

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

object FolderHelper {

    /**
     * 폴더 이름을 입력받는 다이얼로그를 보여주는 함수입니다.
     * @param context 화면을 띄울 액티비티나 프래그먼트의 컨텍스트
     * @param onFolderCreated 폴더가 만들어졌을 때 실행할 동작 (이름을 전달)
     */
    fun showAddFolderDialog(context: Context, onFolderCreated: (String) -> Unit) {
        val editText = EditText(context)
        editText.hint = "새 폴더 이름"
        editText.setPadding(40, 40, 40, 40) // 입력창 주변 여백

        AlertDialog.Builder(context)
            .setTitle("새 폴더 만들기")
            .setView(editText)
            .setPositiveButton("확인") { _, _ ->
                val folderName = editText.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    // 사용자가 입력한 이름을 전달해 줍니다.
                    onFolderCreated(folderName)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}