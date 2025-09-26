package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class QuizFragment : Fragment() {

    private lateinit var spinnerSubject: Spinner
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var etAnswer: EditText
    private lateinit var tvCorrectAnswer: TextView
    private lateinit var btnShowAnswer: Button
    private lateinit var btnNext: Button
    private lateinit var btnManageQuestions: Button

    private lateinit var sharedPref: SharedPreferences
    private val gson = Gson()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val allQuestions = mutableListOf<Question>()
    private val currentSessionQuestions = mutableListOf<Question>()
    private var currentQuestionIndex = 0
    private var currentQuestion: Question? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        sharedPref = requireActivity().getSharedPreferences("quiz_pref", Context.MODE_PRIVATE)

        loadQuestions()
        setupSubjectSpinner()
        setupButtons()

        // 초기 샘플 문제 생성 (첫 실행 시)
        if (allQuestions.isEmpty()) {
            createSampleQuestions()
        }

        startQuizSession()
    }

    private fun initViews(view: View) {
        spinnerSubject = view.findViewById(R.id.spinner_subject)
        tvQuestionNumber = view.findViewById(R.id.tv_question_number)
        tvDifficulty = view.findViewById(R.id.tv_difficulty)
        tvQuestion = view.findViewById(R.id.tv_question)
        etAnswer = view.findViewById(R.id.et_answer)
        tvCorrectAnswer = view.findViewById(R.id.tv_correct_answer)
        btnShowAnswer = view.findViewById(R.id.btn_show_answer)
        btnNext = view.findViewById(R.id.btn_next)
        btnManageQuestions = view.findViewById(R.id.btn_manage_questions)
    }

    private fun setupSubjectSpinner() {
        val subjects = listOf("전체", "국어", "수학", "영어", "과학", "사회")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubject.adapter = adapter

        spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                startQuizSession()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupButtons() {
        btnShowAnswer.setOnClickListener {
            showAnswer()
        }

        btnNext.setOnClickListener {
            nextQuestion()
        }

        btnManageQuestions.setOnClickListener {
            showManageQuestionsDialog()
        }
    }

    private fun startQuizSession() {
        val selectedSubject = spinnerSubject.selectedItem?.toString() ?: "전체"

        currentSessionQuestions.clear()

        val filtered = if (selectedSubject == "전체") {
            allQuestions
        } else {
            allQuestions.filter { it.subject == selectedSubject }
        }

        if (filtered.isNotEmpty()) {
            val questionCount = minOf(10, filtered.size)
            currentSessionQuestions.addAll(filtered.shuffled().take(questionCount))
            currentQuestionIndex = 0
            displayQuestion()
        } else {
            showNoQuestionsMessage()
        }
    }

    private fun displayQuestion() {
        if (currentQuestionIndex < currentSessionQuestions.size) {
            currentQuestion = currentSessionQuestions[currentQuestionIndex]
            currentQuestion?.let { q ->
                tvQuestionNumber.text = "문제 ${currentQuestionIndex + 1}/${currentSessionQuestions.size}"
                tvDifficulty.text = "난이도: ${"★".repeat(q.difficulty)}${"☆".repeat(3 - q.difficulty)}"
                tvQuestion.text = q.question
                etAnswer.text.clear()
                tvCorrectAnswer.visibility = View.GONE
                btnShowAnswer.isEnabled = true
            }
        } else {
            showSessionComplete()
        }
    }

    private fun showAnswer() {
        currentQuestion?.let { q ->
            tvCorrectAnswer.text = "정답: ${q.answer}"
            tvCorrectAnswer.visibility = View.VISIBLE

            val userAnswer = etAnswer.text.toString().trim()
            if (userAnswer.equals(q.answer, ignoreCase = true)) {
                Toast.makeText(context, "정답입니다! 🎉", Toast.LENGTH_SHORT).show()
                q.correctCount++
            } else {
                Toast.makeText(context, "틀렸습니다. 다시 확인해보세요.", Toast.LENGTH_SHORT).show()
                q.incorrectCount++
            }

            q.lastReviewDate = System.currentTimeMillis()
            saveQuestions()
            btnShowAnswer.isEnabled = false
        }
    }

    private fun nextQuestion() {
        currentQuestionIndex++
        displayQuestion()
    }

    private fun showNoQuestionsMessage() {
        tvQuestion.text = "등록된 문제가 없습니다.\n'문제 관리' 버튼을 눌러 문제를 추가해주세요."
        btnShowAnswer.isEnabled = false
        btnNext.isEnabled = false
    }

    private fun showSessionComplete() {
        tvQuestion.text = "모든 문제를 완료했습니다!\n다시 시작하려면 과목을 선택해주세요."
        btnShowAnswer.isEnabled = false
        btnNext.isEnabled = false
    }

    private fun showManageQuestionsDialog() {
        val options = arrayOf("문제 추가", "문제 목록 보기", "샘플 문제 추가", "구글 시트에서 가져오기")

        AlertDialog.Builder(requireContext())
            .setTitle("문제 관리")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddQuestionDialog()
                    1 -> showQuestionListDialog()
                    2 -> createSampleQuestions()
                    3 -> showImportFromSheetsDialog() // 구글 시트 가져오기
                }
            }
            .show()
    }

    private fun showImportFromSheetsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_import_sheets, null)
        val etSheetId = dialogView.findViewById<EditText>(R.id.et_sheet_id)

        AlertDialog.Builder(requireContext())
            .setTitle("구글 시트에서 문제 가져오기")
            .setMessage("구글 스프레드시트 ID를 입력하세요\n(URL에서 /d/ 다음 부분)")
            .setView(dialogView)
            .setPositiveButton("가져오기") { _, _ ->
                val sheetId = etSheetId.text.toString().trim()
                if (sheetId.isNotEmpty()) {
                    importFromGoogleSheets(sheetId)
                } else {
                    Toast.makeText(context, "시트 ID를 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun importFromGoogleSheets(sheetId: String) {
        coroutineScope.launch {
            try {
                val progressDialog = AlertDialog.Builder(requireContext())
                    .setMessage("문제를 가져오는 중...")
                    .setCancelable(false)
                    .create()
                progressDialog.show()

                withContext(Dispatchers.IO) {
                    // 공개 스프레드시트를 CSV로 가져오기
                    val url = "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv"
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val csvContent = response.body?.string() ?: ""
                        val questions = parseCSV(csvContent)

                        withContext(Dispatchers.Main) {
                            if (questions.isNotEmpty()) {
                                allQuestions.addAll(questions)
                                saveQuestions()
                                Toast.makeText(context, "${questions.size}개의 문제를 가져왔습니다", Toast.LENGTH_LONG).show()
                                startQuizSession()
                            } else {
                                Toast.makeText(context, "문제를 가져올 수 없습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "시트를 불러올 수 없습니다. 공개 설정을 확인해주세요", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                progressDialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseCSV(csv: String): List<Question> {
        val questions = mutableListOf<Question>()
        val lines = csv.split("\n")

        // 첫 줄은 헤더라고 가정 (과목,단원,문제,정답,난이도)
        for (i in 1 until lines.size) {
            val parts = lines[i].split(",").map { it.trim() }
            if (parts.size >= 4) {
                questions.add(
                    Question(
                        subject = parts[0],
                        chapter = parts.getOrNull(1) ?: "",
                        question = parts[2],
                        answer = parts[3],
                        difficulty = parts.getOrNull(4)?.toIntOrNull() ?: 1,
                        isUserCreated = false
                    )
                )
            }
        }
        return questions
    }

    private fun showAddQuestionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_question, null)
        val spinnerSubject = dialogView.findViewById<Spinner>(R.id.spinner_dialog_subject)

        // 과목 스피너 설정
        val subjects = listOf("국어", "수학", "영어", "과학", "사회")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubject.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("문제 추가")
            .setView(dialogView)
            .setPositiveButton("추가") { _, _ ->
                val subject = spinnerSubject.selectedItem.toString()
                val question = dialogView.findViewById<EditText>(R.id.et_dialog_question).text.toString()
                val answer = dialogView.findViewById<EditText>(R.id.et_dialog_answer).text.toString()

                if (question.isNotEmpty() && answer.isNotEmpty()) {
                    addQuestion(Question(
                        subject = subject,
                        chapter = "",
                        question = question,
                        answer = answer,
                        difficulty = 2
                    ))
                    Toast.makeText(context, "문제가 추가되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showQuestionListDialog() {
        val questions = allQuestions.map { "${it.subject} - ${it.question}" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("문제 목록")
            .setItems(questions) { _, which ->
                val selectedQuestion = allQuestions[which]
                showQuestionDetailDialog(selectedQuestion)
            }
            .show()
    }

    private fun showQuestionDetailDialog(question: Question) {
        AlertDialog.Builder(requireContext())
            .setTitle("문제 상세")
            .setMessage("문제: ${question.question}\n정답: ${question.answer}\n정답률: ${question.correctCount}/${question.correctCount + question.incorrectCount}")
            .setPositiveButton("닫기", null)
            .setNegativeButton("삭제") { _, _ ->
                allQuestions.remove(question)
                saveQuestions()
                Toast.makeText(context, "문제가 삭제되었습니다", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun createSampleQuestions() {
        val samples = listOf(
            Question(subject = "수학", chapter = "방정식", question = "x + 5 = 12일 때, x의 값은?", answer = "7", difficulty = 1),
            Question(subject = "영어", chapter = "단어", question = "Apple의 한국어 뜻은?", answer = "사과", difficulty = 1),
            Question(subject = "국어", chapter = "맞춤법", question = "'되'와 '돼' 중 맞는 표현은? '내일 비가 ( )려나?'", answer = "되", difficulty = 2),
            Question(subject = "과학", chapter = "물리", question = "물의 끓는점은 몇 도일까요? (섭씨)", answer = "100", difficulty = 1),
            Question(subject = "사회", chapter = "한국사", question = "조선을 건국한 왕은?", answer = "이성계", difficulty = 2)
        )

        allQuestions.addAll(samples)
        saveQuestions()
        Toast.makeText(context, "샘플 문제가 추가되었습니다", Toast.LENGTH_SHORT).show()
        startQuizSession()
    }

    private fun addQuestion(question: Question) {
        allQuestions.add(question)
        saveQuestions()
    }

    private fun saveQuestions() {
        val json = gson.toJson(allQuestions)
        sharedPref.edit().putString("questions_list", json).apply()
    }

    private fun loadQuestions() {
        val json = sharedPref.getString("questions_list", null)
        if (json != null) {
            val type = object : TypeToken<List<Question>>() {}.type
            val questions: List<Question> = gson.fromJson(json, type)
            allQuestions.clear()
            allQuestions.addAll(questions)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}