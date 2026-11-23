package com.example.wheretogo.test


import kotlinx.coroutines.*
import java.io.File
import kotlin.system.measureTimeMillis

object IoExperiment {

    private const val FILE_COUNT = 10
    private const val FILE_SIZE = 1024 * 1024 // 1 MB

    fun createTestFiles(dir: File) {
        dir.mkdirs()
        repeat(FILE_COUNT) { i ->
            File(dir, "session_$i.dat").writeBytes(ByteArray(FILE_SIZE) { it.toByte() })
        }
    }

     fun runSync(dir: File): Long {
        return measureTimeMillis {
            repeat(FILE_COUNT) { i ->
                File(dir, "session_$i.dat").readBytes()
            }
        }
    }

    suspend fun runConcurrent(dir: File, concurrency: Int): Long {
        return measureTimeMillis {
            coroutineScope {
                List(FILE_COUNT) { i ->
                    async(Dispatchers.IO) {
                        File(dir, "session_$i.dat").readBytes()
                    }
                }.chunked(concurrency).forEach { batch ->
                    batch.awaitAll()
                }
            }
        }
    }
}