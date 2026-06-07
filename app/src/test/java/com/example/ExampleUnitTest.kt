package com.example

import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testScrapeYapiKrediRealistic() {
    try {
      println("DEBUG_YAPIKREDI_ALTIN_SEARCH_START")
      val doc = Jsoup.connect("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri/")
          .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
          .get()
      
      val elements = doc.getElementsContainingOwnText("Altın (gram)")
      for (e in elements) {
          println("ALTIN ELEMENT: " + e.tagName() + " TEXT: " + e.text())
          val row = e.parent()?.parent() // Assuming td -> tr
          if (row != null) {
              println("ROW FOUND: " + row.text())
              val tds = row.select("td")
              for (td in tds) {
                  println("TD: " + td.text())
              }
          }
          println("---")
      }
      println("DEBUG_YAPIKREDI_ALTIN_SEARCH_END")
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
