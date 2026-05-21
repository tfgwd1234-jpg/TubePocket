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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI 요소 연결
        // 1단계에서 추가한 전체 화면(mainLayout)을 찾아옵니다.
        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mainLayout)
        val btnAddLink = findViewById<CardView>(R.id.btnAddLink)
        tvMenuHome = findViewById(R.id.tvMenuHome)
        tvMenuStorage = findViewById(R.id.tvMenuStorage)

        // [화면 겹침 해결 코드] 기기의 상태바와 네비게이션바 영역을 계산해서 안쪽으로 밀어줍니다.
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 처음 앱을 실행했을 때 홈 화면(HomeFragment)을 띄움
        replaceFragment(HomeFragment())
        updateMenuUI(isHomeActive = true)

        // 1. 하단 메뉴 '홈' 클릭 이벤트
        tvMenuHome.setOnClickListener {
            replaceFragment(HomeFragment())
            updateMenuUI(isHomeActive = true)
        }

        // 2. 하단 메뉴 '보관함' 클릭 이벤트
        tvMenuStorage.setOnClickListener {
            replaceFragment(StorageFragment())
            updateMenuUI(isHomeActive = false)
        }

        // 3. 중앙 (+) 버튼 클릭 이벤트 (모달 띄우기)
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

    // 클릭된 메뉴에 따라 색상과 글씨 굵기를 변경하는 함수
    private fun updateMenuUI(isHomeActive: Boolean) {
        val activeColor = ContextCompat.getColor(this, R.color.main_red)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_gray)

        if (isHomeActive) {
            tvMenuHome.setTextColor(activeColor)
            tvMenuHome.setTypeface(null, Typeface.BOLD)
            tvMenuStorage.setTextColor(inactiveColor)
            tvMenuStorage.setTypeface(null, Typeface.NORMAL)
        } else {
            tvMenuHome.setTextColor(inactiveColor)
            tvMenuHome.setTypeface(null, Typeface.NORMAL)
            tvMenuStorage.setTextColor(activeColor)
            tvMenuStorage.setTypeface(null, Typeface.BOLD)
        }
    }
}