package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 상태 바 설정 (한 번만 호출)
        setupStatusBar()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 시작 시 HomeFragment 표시
        replaceFragment(HomeFragment())

        // 네비게이션 클릭 리스너
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.nav_timetable -> {
                    replaceFragment(TimetableFragment())
                    true
                }
                R.id.nav_timer -> {
                    replaceFragment(TimerFragment())
                    true
                }
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_quiz -> {
                    replaceFragment(QuizFragment())
                    true
                }
                R.id.nav_note -> {
                    replaceFragment(NoteFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupStatusBar() {
        // 전체 화면 모드 해제
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // 상태 바 아이콘을 검은색으로 설정
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // 상태 바 투명 배경
        window.statusBarColor = getColor(android.R.color.transparent)
    }
}