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
    private lateinit var tvMenuFavorite: TextView // 👈 [추가 1] 즐겨찾기 메뉴 변수 추가
    // 👈 [추가] 뒤로가기 누른 시간을 기억하는 똑똑한 초시계 변수
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 👇 [요청 5번 반영] 물리 뒤로가기 버튼 행동 규칙 정의 (여기에 통째로 추가하세요)
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 현재 화면이 무슨 화면인지 검사
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

                if (currentFragment is HomeFragment) {
                    // 홈 화면이라면: 2초 안에 두 번 눌러야 완전히 종료됨
                    if (System.currentTimeMillis() - backPressedTime < 2000) {
                        finish() // 앱 완전 종료
                    } else {
                        android.widget.Toast.makeText(this@MainActivity, "뒤로 버튼 한 번 더 누르시면 종료됩니다", android.widget.Toast.LENGTH_SHORT).show()
                        backPressedTime = System.currentTimeMillis() // 시간 저장
                    }
                } else {
                    // 홈 화면이 아니면(보관함, 태그 등) 무조건 홈 화면으로 이동!
                    replaceFragment(HomeFragment())
                    updateMenuUI("HOME")
                }
            }
        })

        // UI 요소 연결
        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.mainLayout)
        val btnAddLink = findViewById<CardView>(R.id.btnAddLink)
        tvMenuHome = findViewById(R.id.tvMenuHome)
        tvMenuStorage = findViewById(R.id.tvMenuStorage)
        tvMenuTag = findViewById(R.id.tvMenuTag) // 👈 태그 메뉴 뷰 찾기 연결
        tvMenuFavorite = findViewById(R.id.tvMenuFavorite) // 👈 [추가 2] XML에서 만든 아이디랑 연결

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
        // 👈 [추가 3] 하단 메뉴 '즐겨찾기' 버튼을 눌렀을 때 작동할 클릭 이벤트 추가!
        tvMenuFavorite.setOnClickListener {
            replaceFragment(FavoriteFragment()) // 즐겨찾기 화면으로 교체
            updateMenuUI("FAVORITE") // 선택된 메뉴 색상을 변경
        }

        // 4. 중앙 (+) 버튼 클릭 이벤트 (모달 띄우기)
        btnAddLink.setOnClickListener {
            val bottomSheet = AddLinkBottomSheetFragment()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
        // 👇 여기부터 추가하세요 👇
        // 유튜브 등에서 '공유하기'를 통해 앱이 열렸을 때 새 영상 추가 창을 자동으로 띄웁니다.
        if (intent?.action == android.content.Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT) // 👈 전달된 링크 꺼내기
            val bottomSheet = AddLinkBottomSheetFragment()

            if (sharedText != null) {
                bottomSheet.setSharedLink(sharedText) // 👈 바텀시트(입력창)로 링크 건네주기
            }

            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }
        // 👆 여기까지 추가하세요 👆
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

        // 👈 [추가 4] 즐겨찾기 글씨도 기본 상태(회색)로 되돌려놓는 코드 추가
        tvMenuFavorite.setTextColor(inactiveColor)
        tvMenuFavorite.setTypeface(null, Typeface.NORMAL)

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
            "FAVORITE" -> { // 👈 [추가 5] 즐겨찾기를 선택했을 때 빨갛게 변하도록 추가
                tvMenuFavorite.setTextColor(activeColor)
                tvMenuFavorite.setTypeface(null, Typeface.BOLD)
            }
        }
    }
}