package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        window.navigationBarColor = Color.BLACK  // 네비게이션 바 파란색
        // 상태바 검은색으로 설정

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

        // 홈 Fragment 표시 및 홈 탭 선택 (리스너 설정 후에 실행)
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}