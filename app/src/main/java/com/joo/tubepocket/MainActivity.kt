package com.joo.tubepocket

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var tvMenuHome: TextView
    private lateinit var tvMenuStorage: TextView
    private lateinit var tvMenuTag: TextView // 👈 태그 메뉴 변수 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI 요소 연결
        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mainLayout)
        val btnAddLink = findViewById<CardView>(R.id.btnAddLink)
        tvMenuHome = findViewById(R.id.tvMenuHome)
        tvMenuStorage = findViewById(R.id.tvMenuStorage)
        tvMenuTag = findViewById(R.id.tvMenuTag) // 👈 태그 메뉴 뷰 찾기 연결

        // [화면 겹침 해결 코드]
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 처음 앱을 실행했을 때 홈 화면(HomeFragment)을 띄움
        replaceFragment(HomeFragment())
        updateMenuUI("HOME") // 👈 방식 변경 (3개 메뉴 대응)

        // 1. 하단 메뉴 '홈' 클릭 이벤트
        tvMenuHome.setOnClickListener {
            replaceFragment(HomeFragment())
            updateMenuUI("HOME")
        }

        // 2. 하단 메뉴 '보관함' 클릭 이벤트
        tvMenuStorage.setOnClickListener {
            replaceFragment(StorageFragment())
            updateMenuUI("STORAGE")
        }

        // 3. 하단 메뉴 '태그관리' 클릭 이벤트 (새로 추가됨!)
        tvMenuTag.setOnClickListener {
            replaceFragment(TagFragment())
            updateMenuUI("TAG")
        }

        // 4. 중앙 (+) 버튼 클릭 이벤트 (모달 띄우기)
        btnAddLink.setOnClickListener {
            val bottomSheet = AddLinkBottomSheetFragment()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
    }

    // 프래그먼트를 교체하는 함수
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // [수정됨] 클릭된 메뉴에 따라 색상과 글씨 굵기를 변경하는 함수 (3개 지원)
    fun updateMenuUI(activeMenu: String) {
        val activeColor = ContextCompat.getColor(this, R.color.main_red)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_gray)

        // 1. 먼저 모든 글씨 색상을 회색(기본)으로 되돌립니다.
        tvMenuHome.setTextColor(inactiveColor)
        tvMenuHome.setTypeface(null, Typeface.NORMAL)
        tvMenuStorage.setTextColor(inactiveColor)
        tvMenuStorage.setTypeface(null, Typeface.NORMAL)
        tvMenuTag.setTextColor(inactiveColor)
        tvMenuTag.setTypeface(null, Typeface.NORMAL)

        // 2. 선택된 메뉴만 빨간색과 두꺼운 글씨로 바꿔줍니다.
        when (activeMenu) {
            "HOME" -> {
                tvMenuHome.setTextColor(activeColor)
                tvMenuHome.setTypeface(null, Typeface.BOLD)
            }
            "STORAGE" -> {
                tvMenuStorage.setTextColor(activeColor)
                tvMenuStorage.setTypeface(null, Typeface.BOLD)
            }
            "TAG" -> {
                tvMenuTag.setTextColor(activeColor)
                tvMenuTag.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}