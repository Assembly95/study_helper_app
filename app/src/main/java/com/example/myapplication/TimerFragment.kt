package com.example.myapplication

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class TimerFragment : Fragment() {

    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnReset: Button
    private lateinit var btnModeTimer: Button
    private lateinit var btnModePomodoro: Button

    private lateinit var layoutTimePicker: LinearLayout
    private lateinit var layoutPomodoroPresets: LinearLayout
    private lateinit var pickerHours: NumberPicker
    private lateinit var pickerMinutes: NumberPicker
    private lateinit var pickerSeconds: NumberPicker

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var isTimerRunning = false
    private var isGeneralTimerMode = true // true: 일반 타이머, false: 뽀모도로

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupNumberPickers()
        setupClickListeners()
        updateTimerDisplay()
    }

    private fun initViews(view: View) {
        tvTimer = view.findViewById(R.id.tv_timer)
        btnStart = view.findViewById(R.id.btn_start)
        btnPause = view.findViewById(R.id.btn_pause)
        btnReset = view.findViewById(R.id.btn_reset)
        btnModeTimer = view.findViewById(R.id.btn_mode_timer)
        btnModePomodoro = view.findViewById(R.id.btn_mode_pomodoro)

        layoutTimePicker = view.findViewById(R.id.layout_time_picker)
        layoutPomodoroPresets = view.findViewById(R.id.layout_pomodoro_presets)
        pickerHours = view.findViewById(R.id.picker_hours)
        pickerMinutes = view.findViewById(R.id.picker_minutes)
        pickerSeconds = view.findViewById(R.id.picker_seconds)
    }

    private fun setupNumberPickers() {
        pickerHours.minValue = 0
        pickerHours.maxValue = 23
        pickerMinutes.minValue = 0
        pickerMinutes.maxValue = 59
        pickerSeconds.minValue = 0
        pickerSeconds.maxValue = 59

        pickerHours.setOnValueChangedListener { _, _, newVal ->
            if (!isTimerRunning) {
                updateTimeFromPickers()
            }
        }
        pickerMinutes.setOnValueChangedListener { _, _, newVal ->
            if (!isTimerRunning) {
                updateTimeFromPickers()
            }
        }
        pickerSeconds.setOnValueChangedListener { _, _, newVal ->
            if (!isTimerRunning) {
                updateTimeFromPickers()
            }
        }
    }

    private fun setupClickListeners() {
        // 모드 전환 버튼
        btnModeTimer.setOnClickListener {
            isGeneralTimerMode = true
            layoutTimePicker.visibility = View.VISIBLE
            layoutPomodoroPresets.visibility = View.GONE
            resetTimer()
        }

        btnModePomodoro.setOnClickListener {
            isGeneralTimerMode = false
            layoutTimePicker.visibility = View.GONE
            layoutPomodoroPresets.visibility = View.VISIBLE
            timeLeftInMillis = 1500000 // 25분
            resetTimer()
        }

        // 컨트롤 버튼
        btnStart.setOnClickListener {
            if (timeLeftInMillis > 0) {
                startTimer()
            }
        }

        btnPause.setOnClickListener {
            pauseTimer()
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        // 뽀모도로 프리셋 버튼
        view?.findViewById<Button>(R.id.btn_25min)?.setOnClickListener {
            timeLeftInMillis = 1500000 // 25분
            updateTimerDisplay()
        }

        view?.findViewById<Button>(R.id.btn_5min)?.setOnClickListener {
            timeLeftInMillis = 300000 // 5분
            updateTimerDisplay()
        }

        view?.findViewById<Button>(R.id.btn_10min)?.setOnClickListener {
            timeLeftInMillis = 600000 // 10분
            updateTimerDisplay()
        }

        // 초기 모드 설정
        btnModeTimer.performClick()
    }

    private fun updateTimeFromPickers() {
        val hours = pickerHours.value
        val minutes = pickerMinutes.value
        val seconds = pickerSeconds.value
        timeLeftInMillis = ((hours * 3600 + minutes * 60 + seconds) * 1000).toLong()
        updateTimerDisplay()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerDisplay()
            }

            override fun onFinish() {
                isTimerRunning = false
                btnStart.isEnabled = true
                btnPause.isEnabled = false
                // 알림음 추가 가능
            }
        }.start()

        isTimerRunning = true
        btnStart.isEnabled = false
        btnPause.isEnabled = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStart.isEnabled = true
        btnPause.isEnabled = false
    }

    private fun resetTimer() {
        pauseTimer()
        if (isGeneralTimerMode) {
            timeLeftInMillis = 0
            pickerHours.value = 0
            pickerMinutes.value = 0
            pickerSeconds.value = 0
        } else {
            timeLeftInMillis = 1500000 // 뽀모도로 기본값 25분
        }
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = ((timeLeftInMillis / 1000) % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}