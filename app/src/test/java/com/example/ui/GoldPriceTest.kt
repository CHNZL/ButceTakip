package com.example.ui

import org.junit.Test
import org.jsoup.Jsoup

class GoldPriceTest {
    @Test
    fun parseGoldPrices() {
        val doc = Jsoup.connect("https://www.sivaskuyumder.org.tr/").get()
        println("Title: " + doc.title())
        // let's print all things with gold names to find out their structure
        val elems = doc.body().getAllElements()
        val text = doc.body().text()
        println("Text: " + text.take(500))
        
        val rows = doc.select(".table, tr, .card, div")
        for(row in rows) {
            val t = row.text()
            if(t.contains("22 AYAR BİLEZİK")) {
                println("Found 22 AYAR BİLEZİK: " + t)
                println("HTML: " + row.outerHtml().take(300))
            }
        }
    }
}
