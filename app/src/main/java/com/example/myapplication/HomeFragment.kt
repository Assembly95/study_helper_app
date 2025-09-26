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
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvQuote: TextView
    private lateinit var tvQuoteAuthor: TextView
    private lateinit var tvWeather: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvWeatherIcon: TextView
    private lateinit var btnAddTodo: Button
    private lateinit var todoContainer: LinearLayout
    private lateinit var tvNoTodo: TextView

    private lateinit var sharedPref: SharedPreferences
    private val todoList = mutableListOf<String>()

    private fun setupQuote() {
        val quotesText = resources.getStringArray(R.array.quotes_text)
        val quotesAuthor = resources.getStringArray(R.array.quotes_author)

        // 랜덤 인덱스 선택
        val randomIndex = (0 until quotesText.size).random()

        tvQuote.text = "\"${quotesText[randomIndex]}\""
        tvQuoteAuthor.text = "- ${quotesAuthor[randomIndex]}"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        sharedPref = requireActivity().getSharedPreferences("home_pref", Context.MODE_PRIVATE)

        setupGreeting()
        setupDate()
        setupQuote()
        setupWeather()
        loadTodoList()

        btnAddTodo.setOnClickListener {
            showAddTodoDialog()
        }
    }

    private fun initViews(view: View) {
        tvGreeting = view.findViewById(R.id.tv_greeting)
        tvDate = view.findViewById(R.id.tv_date)
        tvQuote = view.findViewById(R.id.tv_quote)
        tvQuoteAuthor = view.findViewById(R.id.tv_quote_author)
        tvWeather = view.findViewById(R.id.tv_weather)
        tvTemperature = view.findViewById(R.id.tv_temperature)
        tvWeatherIcon = view.findViewById(R.id.tv_weather_icon)
        btnAddTodo = view.findViewById(R.id.btn_add_todo)
        todoContainer = view.findViewById(R.id.todo_container)
        tvNoTodo = view.findViewById(R.id.tv_no_todo)
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when(hour) {
            in 5..11 -> "좋은 아침이에요! 📚"
            in 12..16 -> "오늘도 열공하세요! 💪"
            in 17..20 -> "오늘 하루도 수고했어요! 👏"
            else -> "푹 쉬세요! 😴"
        }
        tvGreeting.text = greeting
    }

    private fun setupDate() {
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.KOREAN)
        tvDate.text = dateFormat.format(Date())
    }

    private fun setupWeather() {
        val weatherService = WeatherService()

        weatherService.getWeather("Seoul") { weather ->
            activity?.runOnUiThread {
                if (weather != null && weather.main != null) {
                    // 실제 날씨 데이터 표시
                    tvTemperature.text = "${weather.main.temp.toInt()}°C"
                    tvWeather.text = weather.weather.firstOrNull()?.description ?: "날씨 정보 없음"

                    // 날씨에 따른 이모지
                    val weatherIcon = when(weather.weather.firstOrNull()?.main) {
                        "Clear" -> "☀️"
                        "Clouds" -> "☁️"
                        "Rain" -> "🌧️"
                        "Snow" -> "❄️"
                        "Thunderstorm" -> "⛈️"
                        "Drizzle" -> "🌦️"
                        "Mist", "Fog" -> "🌫️"
                        else -> "🌤️"
                    }
                    tvWeatherIcon.text = weatherIcon
                } else {
                    // 오류 시 기본값 또는 더미 데이터
                    tvTemperature.text = "20°C"
                    tvWeather.text = "맑음"
                    tvWeatherIcon.text = "☀️"
                }
            }
        }
    }

    private fun showAddTodoDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("할 일 추가")

        val input = EditText(requireContext())
        input.hint = "할 일을 입력하세요"
        builder.setView(input)

        builder.setPositiveButton("추가") { _, _ ->
            val todo = input.text.toString()
            if (todo.isNotEmpty()) {
                addTodoItem(todo)
            }
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addTodoItem(todo: String) {
        todoList.add(todo)
        saveTodoList()
        updateTodoView()
    }

    private fun updateTodoView() {
        todoContainer.removeAllViews()

        if (todoList.isEmpty()) {
            tvNoTodo.visibility = View.VISIBLE
        } else {
            tvNoTodo.visibility = View.GONE
            todoList.forEachIndexed { index, todo ->
                val todoView = createTodoItemView(todo, index)
                todoContainer.addView(todoView)
            }
        }
    }

    private fun createTodoItemView(todo: String, index: Int): View {
        val checkBox = CheckBox(context).apply {
            text = todo
            setPadding(0, 8, 0, 8)
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // 체크하면 잠시 후 삭제
                    postDelayed({
                        todoList.removeAt(todoList.indexOf(todo))
                        saveTodoList()
                        updateTodoView()
                    }, 500)
                }
            }
        }
        return checkBox
    }

    private fun saveTodoList() {
        val editor = sharedPref.edit()
        editor.putStringSet("todos", todoList.toSet())
        editor.apply()
    }

    private fun loadTodoList() {
        val todos = sharedPref.getStringSet("todos", emptySet()) ?: emptySet()
        todoList.clear()
        todoList.addAll(todos)
        updateTodoView()
    }
}