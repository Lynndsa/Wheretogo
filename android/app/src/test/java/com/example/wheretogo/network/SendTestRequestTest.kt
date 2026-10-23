package com.example.wheretogo.network

import kotlinx.coroutines.runBlocking
import org.junit.Test

class SendTestRequestTest {

    @Test
    fun sendToRealServer() = runBlocking {
        // Подставьте адрес вашего хостинга (со слэшем в конце)
        val api = RetrofitClient.createApi("https://wheretogo-00tp.onrender.com/api/v1/custom-places/1")

        val response = api.sendTestData(
            TestRequest(title = "Привет", message = "Тестовое сообщение")
        )

        println("Код: ${response.code()}")
        println("Тело: ${response.body()}")
        if (!response.isSuccessful) {
            println("Ошибка: ${response.errorBody()?.string()}")
        }
    }
    @Test
    fun readParsedJson(): Unit = runBlocking {
        val api = RetrofitClient.createApi("https://wheretogo-00tp.onrender.com/")

        val response = api.getNearbyPlaces(lat = 59.9343, lon = 30.3351, radius = 1)
        var count = 0
        if (response.isSuccessful) {

            response.body()?.forEach {
                count += 1
                println("${it.title}: ${it.description}, ${it.address}") }
        } else {
            println("Ошибка: ${response.code()}")
            println("Тело ошибки: ${response.errorBody()?.string()}")
        }
        println("Количество записей: $count")
    }
}