package com.example.ui

import org.junit.Test
import org.jsoup.Jsoup

class YKBankTest {
    @Test
    fun parseBankRates() {
        val doc = Jsoup.connect("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri/")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            .get()
            
        println("Title: " + doc.title())
        
        // Let's try to find target elements by reading the text
        val text = doc.body().text()
        
        println("Text: " + text.take(1500))
        
        val rows = doc.select(".table, tr, td, .currencyRow")
        var count = 0
        for(row in rows) {
            val t = row.text()
            if(t.contains("USD") || t.contains("EUR") || t.contains("XAU") || t.contains("XAG")) {
                println("Found target: " + t)
                count++
                if (count > 20) break
            }
        }
    }
}
