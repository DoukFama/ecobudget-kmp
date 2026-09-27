package com.example.model

import kotlin.test.Test
import kotlin.test.assertEquals

class YearMonthTest {

  @Test
  fun previous_from_january_rolls_back_to_december_previous_year() {
    val january = YearMonth(year = 2026, month = 0)
    assertEquals(YearMonth(year = 2025, month = 11), january.previous())
  }

  @Test
  fun next_from_december_rolls_over_to_january_next_year() {
    val december = YearMonth(year = 2026, month = 11)
    assertEquals(YearMonth(year = 2027, month = 0), december.next())
  }

  @Test
  fun displayLabel_is_in_french() {
    assertEquals("Août 2026", YearMonth(year = 2026, month = 7).displayLabel)
    assertEquals("Janvier 2025", YearMonth(year = 2025, month = 0).displayLabel)
  }
}
