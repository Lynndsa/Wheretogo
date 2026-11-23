
package com.example.wheretogo.test

import android.content.Context
import android.util.Log
import com.example.wheretogo.test.RegistrationValidationExperiment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

object ExperimentsRunner {

    private const val TAG = "LAB4_EXPERIMENTS"

    suspend fun runAllExperiments(context: Context) {

        Log.d(TAG, "--- ЗАПУСК ЭКСПЕРИМЕНТОВ ПР №4 ---")

        // 1. CPU-bound эксперимент
        Log.d(TAG, "1. CPU-bound эксперимент")

        val passwords =
            CpuExperiment.generatePasswords(CpuExperiment.PASSWORD_COUNT)

        val logicalProcessors =
            Runtime.getRuntime().availableProcessors()

        val parallelisms = linkedSetOf(
            1,
            maxOf(2, logicalProcessors / 2),
            logicalProcessors,
            logicalProcessors * 2,
            4
        ).toList()

        Log.d(TAG, "Количество паролей: ${passwords.size}")
        Log.d(TAG, "Логических процессоров: $logicalProcessors")
        Log.d(TAG, "Значения p: $parallelisms")

        val benchmark = CpuExperiment.runBenchmark(
            passwords,
            parallelisms
        )
        for (warmup in benchmark.warmups) {
            Log.d(TAG, warmup)
        }

        fun logStats(name: String, stats: CpuExperiment.MeasurementStats) {
            Log.d(TAG, "$name")
            Log.d(TAG, "Измерения: ${stats.samples}")
            Log.d(TAG, "Минимум: ${stats.min} мс")
            Log.d(TAG, "Медиана: ${stats.median} мс")
            Log.d(TAG, "Максимум: ${stats.max} мс")
            Log.d(
                TAG,
                "Относительный размах: ${
                    String.format(Locale.US, "%.2f", stats.relativeRange)
                }%"
            )
        }

        logStats("Последовательная версия", benchmark.sequential)

        for ((p, stats) in benchmark.parallel) {

            logStats("Параллельная версия, p=$p", stats)

            val speedup =
                benchmark.sequential.median.toDouble() / stats.median

            val efficiency = speedup / p

            val serialFraction =
                if (p > 1) {
                    (1.0 / speedup - 1.0 / p) / (1.0 - 1.0 / p)
                } else {
                    Double.NaN
                }

            Log.d(TAG, "Ускорение при p=$p: ${
                String.format(Locale.US, "%.2f", speedup)
            }x")

            Log.d(TAG, "Эффективность при p=$p: ${
                String.format(Locale.US, "%.2f", efficiency)
            }")

            Log.d(TAG, "Оценка последовательной части: ${
                String.format(Locale.US, "%.4f", serialFraction)
            }")
        }

        // 2. I/O-bound эксперимент
        Log.d(TAG, "2. I/O-bound эксперимент")

        val dir = File(context.filesDir, "lab4_test_files")
        IoExperiment.createTestFiles(dir)

        val ioSyncTime = IoExperiment.runSync(dir)
        Log.d(TAG, "Синхронное чтение: $ioSyncTime мс")

        val ioConcTime = IoExperiment.runConcurrent(dir, 4)
        Log.d(TAG, "Конкурентное чтение (4 потока): $ioConcTime мс")

        dir.deleteRecursively()

        // 3. Race Condition эксперимент
        Log.d(TAG, "3. Эксперимент с гонкой данных")

        val (unsafeCount, unsafeTime) =
            RaceExperiment.runUnsafe(100_000, 8)

        Log.d(
            TAG,
            "Без синхронизации: счетчик=$unsafeCount, время=$unsafeTime мс"
        )

        if (unsafeCount != 800_000) {
            Log.w(TAG, "ОБНАРУЖЕНА ГОНКА ДАННЫХ")
        }

        val (safeCount, safeTime) =
            RaceExperiment.runSafeAtomic(100_000, 8)

        Log.d(
            TAG,
            "С Atomic: счетчик=$safeCount, время=$safeTime мс"
        )
        // 4. P5 — эксперимент с валидацией регистрации
        Log.d(TAG, "4. P5 — эксперимент с валидацией регистрации")

        try {
            withContext(Dispatchers.Default) {
                RegistrationValidationExperiment.runExperiment()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка эксперимента P5", e)
        }

        Log.d(TAG, "--- ЭКСПЕРИМЕНТЫ ЗАВЕРШЕНЫ ---")

        Log.d(TAG, "--- ЭКСПЕРИМЕНТЫ ЗАВЕРШЕНЫ ---")
    }
}
