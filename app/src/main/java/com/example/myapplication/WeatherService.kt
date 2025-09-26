package com.example.myapplication

import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

data class Weather(
    val main: String,
    val description: String
)

class WeatherService {
    private val client = OkHttpClient()
    private val gson = Gson()

    companion object {
        private const val API_KEY = "YOUR_API_KEY_HERE" // 여기에 API 키 입력
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/weather"
    }

    fun getWeather(city: String = "Seoul", callback: (WeatherResponse?) -> Unit) {
        val url = "$BASE_URL?q=$city&appid=$API_KEY&units=metric&lang=kr"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    try {
                        val weatherResponse = gson.fromJson(body.string(), WeatherResponse::class.java)
                        callback(weatherResponse)
                    } catch (e: Exception) {
                        callback(null)
                    }
                } ?: callback(null)
            }
        })
    }
}