package com.example.wheretogo.tools

import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineProgressTest {
    @Test fun `нет ресурсов к загрузке - 0`() {
        assertEquals(0, OfflineProgress.percent(0, 0))
        assertEquals(0, OfflineProgress.percent(5, 0))
        assertEquals(0, OfflineProgress.percent(5, -1))
    }

    @Test fun `ничего не загружено - 0`() = assertEquals(0, OfflineProgress.percent(0, 10))

    @Test fun `половина`() = assertEquals(50, OfflineProgress.percent(5, 10))

    @Test fun `загружено всё - 100`() = assertEquals(100, OfflineProgress.percent(10, 10))

    @Test fun `дробная часть отбрасывается`() = assertEquals(33, OfflineProgress.percent(1, 3))

    @Test fun `99,9 процента не округляется до 100`() =
        assertEquals(99, OfflineProgress.percent(999, 1000))

    @Test fun `выполнено больше требуемого - не больше 100`() =
        assertEquals(100, OfflineProgress.percent(11, 10))
}
