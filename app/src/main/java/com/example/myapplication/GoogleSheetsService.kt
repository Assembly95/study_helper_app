package com.example.myapplication

import com.example.myapplication.Question  // 또는 그냥 아래처럼
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsRequestInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleSheetsService {
    companion object {
        private const val API_KEY = "YOUR_API_KEY_HERE"  // API 키 입력
        private const val APPLICATION_NAME = "Study Helper App"
    }

    private val sheetsService: Sheets = Sheets.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        null
    ).setApplicationName(APPLICATION_NAME)
        .setGoogleClientRequestInitializer(SheetsRequestInitializer(API_KEY))
        .build()

    suspend fun getQuestions(spreadsheetId: String, range: String = "Sheet1!A:E"): List<Question> {
        return withContext(Dispatchers.IO) {
            try {
                val response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute()

                val values = response.getValues()
                if (values.isNullOrEmpty()) {
                    return@withContext emptyList()
                }

                // 첫 행은 헤더라고 가정 (과목, 단원, 문제, 정답, 난이도)
                values.drop(1).mapNotNull { row ->
                    if (row.size >= 4) {
                        Question(
                            subject = row[0].toString(),
                            chapter = if (row.size > 1) row[1].toString() else "",
                            question = row[2].toString(),
                            answer = row[3].toString(),
                            difficulty = if (row.size > 4) {
                                row[4].toString().toIntOrNull() ?: 1
                            } else 1,
                            isUserCreated = false
                        )
                    } else null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}