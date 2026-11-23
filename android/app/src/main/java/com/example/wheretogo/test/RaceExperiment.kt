package com.example.wheretogo.test

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

object RaceExperiment {

    suspend fun runUnsafe(iterations: Int, threads: Int): Pair<Int, Long> {
        var counter = 0
        val time = measureTimeMillis {
            coroutineScope {
                List(threads) {
                    launch(Dispatchers.Default) {
                        repeat(iterations) {
                            counter++ // ГОНКА ДАННЫХ ЗДЕСЬ
                        }
                    }
                }.joinAll()
            }
        }
        return Pair(counter, time)
    }

    suspend fun runSafeAtomic(iterations: Int, threads: Int): Pair<Int, Long> {
        val counter = AtomicInteger(0)
        val time = measureTimeMillis {
            coroutineScope {
                List(threads) {
                    launch(Dispatchers.Default) {
                        repeat(iterations) {
                            counter.incrementAndGet()
                        }
                    }
                }.joinAll()
            }
        }
        return Pair(counter.get(), time)
    }
}