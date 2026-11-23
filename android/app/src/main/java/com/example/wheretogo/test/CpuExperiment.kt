
package com.example.wheretogo.test

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

object CpuExperiment {

    private const val WARMUP_RUNS = 2
    private const val MEASURE_RUNS = 5
    const val PASSWORD_COUNT = 10_000

    data class MeasurementStats(
        val samples: List<Long>
    ) {
        val min: Long = samples.minOrNull() ?: 0L
        val median: Long = samples.sorted()[samples.size / 2]
        val max: Long = samples.maxOrNull() ?: 0L

        val relativeRange: Double =
            if (median == 0L) 0.0
            else (max - min).toDouble() / median * 100.0
    }

    data class BenchmarkResult(
        val warmups: List<String>,
        val sequential: MeasurementStats,
        val parallel: Map<Int, MeasurementStats>
    )
    // Проверка пароля без создания Regex и дополнительных объектов
    private fun validatePasswordHeavy(password: String): Boolean {
        var result = true

        repeat(50) {
            var hasUppercase = false
            var hasDigit = false
            var hasSpecial = false

            for (char in password) {
                if (char in 'A'..'Z') hasUppercase = true
                if (char in '0'..'9') hasDigit = true
                if (char in "!@#$%^&*") hasSpecial = true
            }

            if (!hasUppercase || !hasDigit || !hasSpecial) {
                result = false
            }
        }

        return result
    }

    fun generatePasswords(count: Int): List<String> {
        return List(count) { "TestPass123!_$it" }
    }

    // Последовательная обработка с подсчётом результатов
    private fun sequentialOnce(passwords: List<String>): Int {
        var validCount = 0

        passwords.forEach {
            if (validatePasswordHeavy(it)) {
                validCount++
            }
        }

        return validCount
    }

    // Параллельная обработка с объединением результатов
    private suspend fun parallelOnce(
        passwords: List<String>,
        parallelism: Int
    ): Int {
        require(parallelism > 0)

        val chunkSize =
            (passwords.size + parallelism - 1) / parallelism

        return coroutineScope {
            (0 until parallelism).map { worker ->
                async(Dispatchers.Default) {
                    val start = worker * chunkSize
                    val end = minOf(start + chunkSize, passwords.size)

                    var validCount = 0

                    for (i in start until end) {
                        if (validatePasswordHeavy(passwords[i])) {
                            validCount++
                        }
                    }

                    validCount
                }
            }.awaitAll().sum()
        }
    }

    // Полный эксперимент с чередованием вариантов
    suspend fun runBenchmark(
        passwords: List<String>,
        parallelisms: List<Int>
    ): BenchmarkResult {

        // Проверяем совпадение результатов до измерений
        val expectedResult = sequentialOnce(passwords)

        for (p in parallelisms) {
            val parallelResult = parallelOnce(passwords, p)

            check(parallelResult == expectedResult) {
                "Результаты не совпадают при p=$p"
            }
        }

        val sequentialSamples = mutableListOf<Long>()
        val parallelSamples = parallelisms.associateWith {
            mutableListOf<Long>()
        }

        // Прогревочные запуски
        val warmupLogs = mutableListOf<String>()

        repeat(WARMUP_RUNS) { warmup ->

            val seqTime = measureTimeMillis {
                sequentialOnce(passwords)
            }

            val parTimes = mutableMapOf<Int, Long>()

            for (p in parallelisms) {
                parTimes[p] = measureTimeMillis {
                    parallelOnce(passwords, p)
                }
            }

            val message = "Прогрев ${warmup + 1}: seq=$seqTime мс, parallel=$parTimes"

            warmupLogs.add(message)
        }
        // Серия измерений: варианты чередуются
        repeat(MEASURE_RUNS) { round ->

            if (round % 2 == 0) {
                val time = measureTimeMillis {
                    sequentialOnce(passwords)
                }
                sequentialSamples.add(time)

                for (p in parallelisms) {
                    val parallelTime = measureTimeMillis {
                        parallelOnce(passwords, p)
                    }
                    parallelSamples.getValue(p).add(parallelTime)
                }
            } else {
                for (p in parallelisms) {
                    val parallelTime = measureTimeMillis {
                        parallelOnce(passwords, p)
                    }
                    parallelSamples.getValue(p).add(parallelTime)
                }

                val time = measureTimeMillis {
                    sequentialOnce(passwords)
                }
                sequentialSamples.add(time)
            }
        }

        return BenchmarkResult(
            warmups = warmupLogs,
            sequential = MeasurementStats(sequentialSamples),
            parallel = parallelSamples.mapValues {
                MeasurementStats(it.value)
            }
        )
    }
}
