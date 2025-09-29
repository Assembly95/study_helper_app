package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class TimetableFragment : Fragment() {

    private lateinit var tableLayout: TableLayout
    private lateinit var sharedPref: SharedPreferences
    private val periods = 7  // 7교시
    private val days = 5     // 월-금

    // 과목별 색상 맵 (파스텔 톤)
    private val subjectColors = mapOf(
        "국어" to "#FFB3BA",
        "수학" to "#BAE1FF",
        "영어" to "#FFFFBA",
        "과학" to "#BAFFC9",
        "사회" to "#E0BBE4",
        "체육" to "#FFDAB9",
        "음악" to "#C9C9FF",
        "미술" to "#FFE4E1"
    )

    // 선택된 색상 저장용
    private val customColors = mutableMapOf<String, String>()

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

        loadCustomColors()
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

        // 빈 칸 (왼쪽 상단) - 시계 아이콘 추가
        val emptyHeader = TextView(context).apply {
            text = "⏰"
            background = createGradientDrawable("#6200EE", "#3700B3")
            gravity = Gravity.CENTER
            setPadding(16, 20, 16, 20)
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(2, 2, 2, 2)
            }
        }
        headerRow.addView(emptyHeader)

        // 요일 헤더 (그라데이션 배경)
        val dayNames = arrayOf("월", "화", "수", "목", "금")
        val dayEmojis = arrayOf("🌙", "🔥", "💧", "🌲", "⭐")  // 요일별 이모지

        for (i in dayNames.indices) {
            val dayHeader = TextView(context).apply {
                text = "${dayEmojis[i]}\n${dayNames[i]}"
                background = createGradientDrawable("#6200EE", "#3700B3")
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(16, 20, 16, 20)
                setTypeface(null, Typeface.BOLD)
                textSize = 14f
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(2, 2, 2, 2)
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

            // 교시 표시 (그라데이션)
            val periodText = TextView(context).apply {
                text = "${period}교시"
                background = createGradientDrawable("#E3F2FD", "#BBDEFB")
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#333333"))
                setTypeface(null, Typeface.BOLD)
                textSize = 13f
                setPadding(12, 16, 12, 16)
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(2, 2, 2, 2)
                }
            }
            tableRow.addView(periodText)

            // 과목 칸들
            for (day in 0 until 5) {
                val subjectCell = TextView(context).apply {
                    text = ""
                    background = createCellDrawable()
                    gravity = Gravity.CENTER
                    setTextColor(Color.parseColor("#333333"))
                    textSize = 12f
                    maxLines = 2
                    isClickable = true
                    isFocusable = true
                    setPadding(8, 16, 8, 16)
                    layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1f
                    ).apply {
                        setMargins(2, 2, 2, 2)
                    }

                    // 클릭 효과 추가
                    setOnClickListener {
                        it.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .withEndAction {
                                it.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                            }
                        showImprovedSubjectDialog(period - 1, day)
                    }

                    tag = "cell_${period-1}_$day"
                }
                tableRow.addView(subjectCell)
            }

            tableLayout.addView(tableRow)
        }
    }

    private fun showImprovedSubjectDialog(period: Int, day: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_subject_input_custom, null)
        val editText = dialogView.findViewById<EditText>(R.id.edit_subject_name)
        val colorContainer = dialogView.findViewById<LinearLayout>(R.id.color_picker_container)

        // 기존 과목명 가져오기
        val existingSubject = sharedPref.getString("subject_${period}_$day", "")
        editText.setText(existingSubject)

        // 색상 선택 버튼들
        val colors = listOf(
            "#FFB3BA", "#BAE1FF", "#FFFFBA", "#BAFFC9",
            "#E0BBE4", "#FFDAB9", "#C9C9FF", "#FFE4E1"
        )

        var selectedColor = customColors[existingSubject] ?: colors[0]

        colors.forEach { color ->
            val colorButton = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    48.dpToPx(),
                    48.dpToPx()
                ).apply {
                    marginEnd = 8.dpToPx()
                }
                background = createCircleDrawable(color)
                setOnClickListener {
                    selectedColor = color
                    // 선택 표시 업데이트 - 모든 버튼 순회
                    for (i in 0 until colorContainer.childCount) {
                        val child = colorContainer.getChildAt(i)
                        val childColor = child.tag as String
                        child.background = if (childColor == color) {
                            createSelectedCircleDrawable(childColor)
                        } else {
                            createCircleDrawable(childColor)
                        }
                    }
                }
                tag = color
            }
            colorContainer.addView(colorButton)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("${period + 1}교시 ${getDayName(day)}요일")
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                val subject = editText.text.toString()
                if (subject.isNotEmpty()) {
                    customColors[subject] = selectedColor
                    saveCustomColors()
                    saveSubject(period, day, subject, selectedColor)
                    updateCell(period, day, subject, selectedColor)
                }
            }
            .setNegativeButton("취소", null)
            .apply {
                if (!existingSubject.isNullOrEmpty()) {
                    setNeutralButton("삭제") { _, _ ->
                        saveSubject(period, day, "", "")
                        updateCell(period, day, "", "")
                    }
                }
            }
            .create()

        dialog.show()
    }

    private fun createGradientDrawable(startColor: String, endColor: String): GradientDrawable {
        return GradientDrawable().apply {
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            colors = intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
            cornerRadius = 12f
        }
    }

    private fun createCellDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 8f
            setStroke(2, Color.parseColor("#E0E0E0"))
        }
    }

    private fun createCircleDrawable(color: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(color))
            setStroke(2, Color.parseColor("#DDDDDD"))
        }
    }

    private fun createSelectedCircleDrawable(color: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(color))
            setStroke(4, Color.parseColor("#6200EE"))
        }
    }

    private fun saveSubject(period: Int, day: Int, subject: String, color: String) {
        with(sharedPref.edit()) {
            putString("subject_${period}_$day", subject)
            putString("color_${period}_$day", color)
            apply()
        }
    }

    private fun updateCell(period: Int, day: Int, subject: String, color: String = "#FFFFFF") {
        val cell = tableLayout.findViewWithTag<TextView>("cell_${period}_$day")
        cell?.apply {
            text = subject

            // 색상 문자열 검증
            val validColor = if (color.isEmpty() || color.isBlank()) "#FFFFFF" else color

            background = if (subject.isEmpty()) {
                createCellDrawable()
            } else {
                GradientDrawable().apply {
                    setColor(Color.parseColor(validColor))
                    cornerRadius = 8f
                    setStroke(1, Color.parseColor("#CCCCCC"))
                }
            }

            // 텍스트 색상 조정 (빈 문자열 체크 추가)
            setTextColor(if (isColorLight(validColor)) Color.BLACK else Color.WHITE)
        }
    }

    private fun loadTimetableData() {
        for (period in 0 until periods) {
            for (day in 0 until days) {
                val subject = sharedPref.getString("subject_${period}_$day", "") ?: ""
                val color = sharedPref.getString("color_${period}_$day", "#F5F5F5") ?: "#F5F5F5"
                if (subject.isNotEmpty()) {
                    updateCell(period, day, subject, color)
                }
            }
        }
    }

    private fun saveCustomColors() {
        val editor = sharedPref.edit()
        customColors.forEach { (subject, color) ->
            editor.putString("custom_color_$subject", color)
        }
        editor.apply()
    }

    private fun loadCustomColors() {
        sharedPref.all.forEach { (key, value) ->
            if (key.startsWith("custom_color_")) {
                val subject = key.removePrefix("custom_color_")
                customColors[subject] = value as String
            }
        }
    }

    private fun getDayName(day: Int): String {
        return when(day) {
            0 -> "월"
            1 -> "화"
            2 -> "수"
            3 -> "목"
            4 -> "금"
            else -> ""
        }
    }

    private fun isColorLight(colorString: String): Boolean {
        // 빈 문자열 체크
        if (colorString.isEmpty() || colorString.isBlank()) {
            return true // 기본값: 밝은 색으로 간주
        }

        return try {
            val color = Color.parseColor(colorString)
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            darkness < 0.5
        } catch (e: Exception) {
            true // 파싱 실패 시 기본값
        }
    }

    // dp to px 변환 확장 함수
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun deleteSubject(period: Int, day: Int) {
        Log.d("TimetableFragment", "삭제 시작: period=$period, day=$day")

        try {
            // 삭제 전 현재 값 확인
            val currentSubject = sharedPref.getString("subject_${period}_$day", "")
            Log.d("TimetableFragment", "현재 과목: $currentSubject")

            // SharedPreferences에서 삭제
            val editor = sharedPref.edit()
            editor.remove("subject_${period}_$day")
            editor.apply()

            // UI 업데이트
            val cell = tableLayout.findViewWithTag<TextView>("cell_${period}_$day")
            if (cell != null) {
                cell.text = ""
                cell.setBackgroundColor(Color.WHITE)
                Log.d("TimetableFragment", "셀 업데이트 완료")
            } else {
                Log.e("TimetableFragment", "셀을 찾을 수 없음: cell_${period}_$day")
            }

        } catch (e: Exception) {
            Log.e("TimetableFragment", "삭제 중 오류", e)
            Toast.makeText(context, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}