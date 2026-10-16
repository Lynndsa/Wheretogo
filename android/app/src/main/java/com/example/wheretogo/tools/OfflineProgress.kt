package com.example.wheretogo.tools

/** Расчёт процента загрузки офлайн-карты (вынесено из OfflineMapManager для тестирования). */
object OfflineProgress {
    /**
     * P1 required <= 0 → 0.
     * P2 Дробная часть отбрасывается: 99,9 % показывается как 99, чтобы 100 означало «загружено полностью».
     * P3 Результат всегда в диапазоне 0..100.
     */
    fun percent(completed: Long, required: Long): Int {
        if (required <= 0) return 0
        return (100.0 * completed / required).toInt().coerceIn(0, 100)
    }
}
