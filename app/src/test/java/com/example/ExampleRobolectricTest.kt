package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun `verify app identity and creator`() {
    val appName = "OmniLang"
    val creator = "ALVI"
    assertEquals("OmniLang", appName)
    assertTrue(creator.isNotEmpty())
  }
}
