package com.example.myapplication

import android.R.attr.textStyle
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.graphics.drawable.GradientDrawable  // 이 import 추가 필요
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment

class TimetableFragment : Fragment() {

    private lateinit var tableLayout: TableLayout
    private lateinit var sharedPref: SharedPreferences
    private val periods = 7  // 7교시
    private val days = 5     // 월-금

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timetable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tableLayout = view.findViewById(R.id.timetable_layout)
        sharedPref = requireActivity().getSharedPreferences("timetable_pref", Context.MODE_PRIVATE)

        createTimetable()
        loadTimetableData()
    }

    private fun createTimetable() {
        // 요일 헤더 추가
        val headerRow = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 빈 칸 (왼쪽 상단)
        val emptyHeader = TextView(context).apply {
            text = ""
            setBackgroundColor(Color.parseColor("#808080"))
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(1, 1, 1, 1)
            }
        }
        headerRow.addView(emptyHeader)

        // 요일 헤더
        val days = arrayOf("월", "화", "수", "목", "금")
        for (dayName in days) {
            val dayHeader = TextView(context).apply {
                text = dayName
                setBackgroundColor(Color.parseColor("#808080"))
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(8, 12, 8, 12)
                setTypeface(null, Typeface.BOLD)
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(1, 1, 1, 1)
                }
            }
            headerRow.addView(dayHeader)
        }

        tableLayout.addView(headerRow)

        // 1-7교시 추가
        for (period in 1..periods) {
            val tableRow = TableRow(context).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }

            // 교시 표시
            val periodText = TextView(context).apply {
                text = "${period}교시"
                setBackgroundColor(Color.parseColor("#E0E0E0"))
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                textSize = 12f
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(1, 1, 1, 1)
                }
            }
            tableRow.addView(periodText)

            // 과목 칸들
            for (day in 0 until 5) {
                val subjectCell = TextView(context).apply {
                    text = ""
                    setBackgroundResource(R.drawable.table_cell_selector)  // 선택자 drawable 사용
                    gravity = Gravity.CENTER
                    setTextColor(Color.BLACK)
                    textSize = 11f
                    maxLines = 3
                    isClickable = true  // 클릭 가능하게 설정
                    isFocusable = true  // 포커스 가능하게 설정
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1f
                    ).apply {
                        setMargins(1, 1, 1, 1)
                    }

                    setOnClickListener {
                        showSubjectInputDialog(period - 1, day)
                    }

                    tag = "cell_${period-1}_$day"
                }
                tableRow.addView(subjectCell)
            }

            tableLayout.addView(tableRow)
        }
    }


    private fun showSubjectInputDialog(period: Int, day: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("과목 입력")

        val input = EditText(requireContext())
        input.hint = "과목명을 입력하세요"

        // 기존 과목명 가져오기
        val existingSubject = sharedPref.getString("subject_${period}_$day", "")
        input.setText(existingSubject)

        builder.setView(input)

        builder.setPositiveButton("확인") { _, _ ->
            val subject = input.text.toString()
            saveSubject(period, day, subject)
            updateCell(period, day, subject)
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.cancel()
        }

        // 삭제 버튼 추가
        if (!existingSubject.isNullOrEmpty()) {
            builder.setNeutralButton("삭제") { _, _ ->
                saveSubject(period, day, "")
                updateCell(period, day, "")
            }
        }

        builder.show()
    }

    private fun saveSubject(period: Int, day: Int, subject: String) {
        with(sharedPref.edit()) {
            putString("subject_${period}_$day", subject)
            apply()
        }
    }

    private fun updateCell(period: Int, day: Int, subject: String) {
        val cell = tableLayout.findViewWithTag<TextView>("cell_${period}_$day")
        cell?.apply {
            text = subject
            // 과목별 색상 설정 (선택사항)
            setBackgroundColor(if (subject.isEmpty()) Color.WHITE else getSubjectColor(subject))
        }
    }

    private fun loadTimetableData() {
        for (period in 0 until periods) {
            for (day in 0 until days) {
                val subject = sharedPref.getString("subject_${period}_$day", "") ?: ""
                if (subject.isNotEmpty()) {
                    updateCell(period, day, subject)
                }
            }
        }
    }

    private fun getSubjectColor(subject: String): Int {
        // 과목명에 따라 다른 색상 반환 (파스텔 톤)
        return when {
            subject.contains("국어") -> Color.parseColor("#FFE5E5")
            subject.contains("수학") -> Color.parseColor("#E5F3FF")
            subject.contains("영어") -> Color.parseColor("#FFF3E5")
            subject.contains("과학") -> Color.parseColor("#E5FFE5")
            subject.contains("사회") -> Color.parseColor("#F3E5FF")
            else -> Color.parseColor("#F5F5F5")
        }
    }
}