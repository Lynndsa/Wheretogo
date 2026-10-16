package com.example.wheretogo.network

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Сеть заменена MockWebServer: тесты проверяют контракт клиента (метод, путь, JSON, разбор ответа,
 * поведение при ошибках), но НЕ проверяют реальный сервер и реальную сеть.
 */
class ApiServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    @Before fun setUp() {
        server = MockWebServer().also { it.start() }
        api = RetrofitClient.createApi(server.url("/").toString())
    }

    @After fun tearDown() = server.shutdown()

    @Test fun `отправляет POST на api-test с JSON-телом`() = runTest {
        server.enqueue(MockResponse().setBody("""{"success":true,"responseMessage":"ok"}"""))

        api.sendTestData(TestRequest(title = "t", message = "m"))

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/test", request.path)
        val sent = Gson().fromJson(request.body.readUtf8(), TestRequest::class.java)
        assertEquals(TestRequest("t", "m"), sent)
    }

    @Test fun `разбирает успешный ответ`() = runTest {
        server.enqueue(MockResponse().setBody("""{"success":true,"responseMessage":"принято"}"""))

        val response = api.sendTestData(TestRequest("t", "m"))

        assertTrue(response.isSuccessful)
        assertEquals(TestResponse(true, "принято"), response.body())
    }

    @Test fun `ответ 500 не бросает исключение и не содержит тела`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":"boom"}"""))

        val response = api.sendTestData(TestRequest("t", "m"))

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
        assertNull(response.body())
    }

    @Test fun `ответ 404 помечается как неуспешный`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val response = api.sendTestData(TestRequest("t", "m"))

        assertFalse(response.isSuccessful)
        assertEquals(404, response.code())
    }

    @Test fun `битый JSON в успешном ответе приводит к исключению`() = runTest {
        server.enqueue(MockResponse().setBody("""{"success":"""))

        val result = runCatching { api.sendTestData(TestRequest("t", "m")) }

        assertTrue(result.isFailure)
    }
}
