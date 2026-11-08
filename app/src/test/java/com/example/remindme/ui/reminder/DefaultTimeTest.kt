package com.example.remindme.ui.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DefaultTimeTest {

    private fun at(hour: Int, minute: Int) = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.NOVEMBER, 4, hour, minute)
    }

    @Test fun `future day gets 9 00`() =
        assertEquals(9 to 0, defaultTimeFor(2026, Calendar.NOVEMBER, 6, now = at(20, 54)))

    @Test fun `today before 9 keeps 9 00`() =
        assertEquals(9 to 0, defaultTimeFor(2026, Calendar.NOVEMBER, 4, now = at(7, 30)))

    @Test fun `today after 9 gets the next whole hour`() =
        assertEquals(21 to 0, defaultTimeFor(2026, Calendar.NOVEMBER, 4, now = at(20, 54)))

    @Test fun `today at exactly 9 moves on to 10`() =
        assertEquals(10 to 0, defaultTimeFor(2026, Calendar.NOVEMBER, 4, now = at(9, 0)))

    @Test fun `last hour of the day gets 23 59`() =
        assertEquals(23 to 59, defaultTimeFor(2026, Calendar.NOVEMBER, 4, now = at(23, 20)))
}
