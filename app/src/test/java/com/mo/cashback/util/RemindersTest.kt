package com.mo.cashback.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The reminder is supposed to land on the 30th of the month at 10:00, and to fall back to the
 * last day of the month when there is no 30th. That date arithmetic is the only real logic in
 * [Reminders], and it is pure — no Android APIs — so it can be covered by plain JVM tests.
 */
class RemindersTest {

    private fun nextFireAt(now: LocalDateTime): LocalDateTime =
        Instant.ofEpochMilli(Reminders.nextFireTimeMillis(now))
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

    @Test
    fun `fires on the 30th of this month while it is still ahead`() {
        assertEquals(
            LocalDateTime.of(2026, 5, 30, 10, 0),
            nextFireAt(LocalDateTime.of(2026, 5, 3, 9, 0)),
        )
    }

    @Test
    fun `rolls over to next month once the 30th has passed`() {
        assertEquals(
            LocalDateTime.of(2026, 6, 30, 10, 0),
            nextFireAt(LocalDateTime.of(2026, 5, 30, 10, 1)),
        )
    }

    @Test
    fun `does not re-schedule for today when it is already exactly 10 00 on the 30th`() {
        assertEquals(
            LocalDateTime.of(2026, 6, 30, 10, 0),
            nextFireAt(LocalDateTime.of(2026, 5, 30, 10, 0)),
        )
    }

    @Test
    fun `clamps to the 28th in a short February`() {
        assertEquals(
            LocalDateTime.of(2026, 2, 28, 10, 0),
            nextFireAt(LocalDateTime.of(2026, 2, 1, 8, 0)),
        )
    }

    @Test
    fun `clamps to the 29th in a leap February`() {
        assertEquals(
            LocalDateTime.of(2028, 2, 29, 10, 0),
            nextFireAt(LocalDateTime.of(2028, 2, 1, 8, 0)),
        )
    }

    @Test
    fun `crosses the year boundary from December`() {
        assertEquals(
            LocalDateTime.of(2027, 1, 30, 10, 0),
            nextFireAt(LocalDateTime.of(2026, 12, 31, 12, 0)),
        )
    }
}
