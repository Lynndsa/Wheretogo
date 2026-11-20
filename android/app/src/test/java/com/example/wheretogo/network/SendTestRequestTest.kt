package com.example.wheretogo.network

import com.example.wheretogo.data.remote.RetrofitClient
import com.example.wheretogo.data.remote.TestRequest
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SendTestRequestTest {

    @Test
    fun sendToRealServer() = runBlocking {
        val api = RetrofitClient.createApi("https://wheretogo-00tp.onrender.com/")

        val response = api.sendTestData(
            TestRequest(title = "Привет", message = "Тестовое сообщение")
        )

        println("Код: ${response.code()}")
        println("Тело: ${response.body()}")
        if (!response.isSuccessful) {
            println("Ошибка: ${response.errorBody()?.string()}")
        }
    }
}