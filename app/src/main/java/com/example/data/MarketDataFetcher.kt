package com.example.data

import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.Locale

data class LiveMarketSnapshot(
    val goldPrices: List<GoldPrice> = emptyList(),
    val yapiKrediRates: List<BankRate> = emptyList(),
    val ziraatRates: List<BankRate> = emptyList()
)

/**
 * Shared live market fetcher used by both BudgetViewModel (UI) and MarketAlertWorker (background).
 */
object MarketDataFetcher {

    fun fetchSivasGold(): List<GoldPrice> {
        val prices = mutableListOf<GoldPrice>()
        try {
            val doc = Jsoup.connect("https://www.sivaskuyumder.org.tr/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .timeout(6000)
                .get()
            val text = doc.body().text()

            Regex("22 Ayar Bile\\s+([\\d,.]+)\\s+([\\d,.]+)").find(text)?.let {
                prices.add(GoldPrice("22 AYAR BİLEZİK", it.groupValues[1], it.groupValues[2]))
            }
            Regex("14 Ayar 1 Gr\\s+([\\d,.]+)").find(text)?.let {
                prices.add(GoldPrice("14 AYAR GRAM ALTIN", "-", it.groupValues[1]))
            }
            Regex("22 Ayar 1 Gr\\s+([\\d,.]+)\\s+([\\d,.]+)").find(text)?.let {
                prices.add(GoldPrice("22 AYAR GRAM ALTIN", it.groupValues[1], it.groupValues[2]))
            }
            Regex("24 Ayar 1 Gr\\s+([\\d,.]+)\\s+([\\d,.]+)").find(text)?.let {
                prices.add(GoldPrice("24 AYAR GRAM ALTIN", it.groupValues[1], it.groupValues[2]))
            }
            Regex("Çeyrek\\s+([\\d,.]+)\\s+([\\d,.]+)\\s+([\\d,.]+)\\s+([\\d,.]+)").find(text)?.let {
                prices.add(GoldPrice("ÇEYREK (YENİ)", it.groupValues[1], it.groupValues[2]))
                prices.add(GoldPrice("ÇEYREK (ESKİ)", it.groupValues[3], it.groupValues[4]))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return prices
    }

    fun fetchYapiKrediRates(): List<BankRate> {
        val rates = mutableListOf<BankRate>()
        var success = false
        try {
            val doc = Jsoup.connect("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .timeout(5000)
                .get()

            var usdBuy = "0,00"
            var usdSell = "0,00"
            var eurBuy = "0,00"
            var eurSell = "0,00"
            var xauBuy = ""
            var xauSell = ""
            var xagBuy = ""
            var xagSell = ""
            var foundAny = false

            val rows = doc.select("table tbody tr")
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size >= 4) {
                    val code = cells[0].text().trim().lowercase(Locale.ROOT)
                    val name = cells[1].text().trim().lowercase(Locale("tr", "TR"))
                    val buy = cells[2].text().trim()
                    val sell = cells[3].text().trim()

                    when {
                        code == "usd" -> {
                            usdBuy = buy; usdSell = sell; foundAny = true
                        }
                        code == "eur" -> {
                            eurBuy = buy; eurSell = sell; foundAny = true
                        }
                        name.contains("altın") || name.contains("altin") || code.contains("xau") -> {
                            xauBuy = buy; xauSell = sell; foundAny = true
                        }
                        name.contains("gümüş") || name.contains("gumus") || code.contains("xag") -> {
                            xagBuy = buy; xagSell = sell; foundAny = true
                        }
                    }
                }
            }

            if (foundAny) {
                rates.add(BankRate("USD", "Amerikan Doları", usdBuy, usdSell))
                rates.add(BankRate("EUR", "Euro", eurBuy, eurSell))
                if (xauBuy.isEmpty()) {
                    xauBuy = "0,00"; xauSell = "0,00"
                }
                rates.add(BankRate("XAU", "Altın (gram)", xauBuy, xauSell))
                if (xagBuy.isEmpty()) {
                    val buyD = MarketPriceParser.parseDoubleOrNull(xauBuy)
                    if (buyD != null && buyD > 0.0) {
                        val est = String.format(Locale("tr"), "%.2f", buyD / 85.0)
                        xagBuy = est; xagSell = est
                    } else {
                        xagBuy = "0,00"; xagSell = "0,00"
                    }
                }
                rates.add(BankRate("XAG", "Gümüş (gram)", xagBuy, xagSell))
                success = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!success || rates.isEmpty()) {
            try {
                val doc = Jsoup.connect("https://www.tcmb.gov.tr/kurlar/today.xml")
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get()
                val currencies = doc.select("Currency")
                var usdBuy = "0,00"
                var usdSell = "0,00"
                var eurBuy = "0,00"
                var eurSell = "0,00"
                for (c in currencies) {
                    if (c.attr("CurrencyCode") == "USD") {
                        usdBuy = c.select("ForexBuying").text().replace(".", ",")
                        usdSell = c.select("ForexSelling").text().replace(".", ",")
                    } else if (c.attr("CurrencyCode") == "EUR") {
                        eurBuy = c.select("ForexBuying").text().replace(".", ",")
                        eurSell = c.select("ForexSelling").text().replace(".", ",")
                    }
                }
                rates.add(BankRate("USD", "Amerikan Doları", usdBuy, usdSell))
                rates.add(BankRate("EUR", "Euro", eurBuy, eurSell))
                rates.add(BankRate("XAU", "Altın (gram)", "0,00", "0,00"))
                rates.add(BankRate("XAG", "Gümüş (gram)", "0,00", "0,00"))
            } catch (_: Exception) {
                // Keep empty if totally offline in background check
            }
        }
        return rates
    }

    fun fetchZiraatRates(): List<BankRate> {
        val rates = mutableListOf<BankRate>()
        try {
            val responseJson = Jsoup.connect("https://www.ziraatbank.com.tr/tr/_layouts/15/Ziraat/HomePage/Ajax.aspx/GetZiraatVerileri")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .header("X-Requested-With", "JQuery PageEvents")
                .header("Content-Type", "core/json")
                .ignoreContentType(true)
                .timeout(6000)
                .execute()
                .body()

            val obj = JSONObject(responseJson)
            val html = obj.getJSONObject("d").getJSONObject("Data").getString("Html")
            if (html.isNotEmpty()) {
                val listItems = Jsoup.parse(html).select("ul li")
                for (li in listItems) {
                    val title = li.select("h2").text().trim()
                    val spans = li.select("span")
                    if (spans.size >= 2) {
                        val buyVal = formatToTwoDecimals(spans[0].text().trim())
                        val sellVal = formatToTwoDecimals(spans[1].text().trim())
                        val (code, name) = when {
                            title.contains("DOLAR", ignoreCase = true) || title.contains("USD", ignoreCase = true) -> "USD" to "Amerikan Doları"
                            title.contains("EURO", ignoreCase = true) || title.contains("EUR", ignoreCase = true) -> "EUR" to "Euro"
                            title.contains("ALTIN", ignoreCase = true) || title.contains("XAU", ignoreCase = true) -> "XAU" to "Altın (1000)"
                            title.contains("GÜMÜŞ", ignoreCase = true) || title.contains("GUMUS", ignoreCase = true) || title.contains("XAG", ignoreCase = true) -> "XAG" to "Gümüş"
                            else -> title to title
                        }
                        rates.add(BankRate(code, name, buyVal, sellVal))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rates
    }

    /**
     * Fetches only the market sources referenced by the given asset keys to minimize network & battery usage.
     */
    fun fetchRequiredSources(assetKeys: Set<String>): LiveMarketSnapshot {
        val needGold = assetKeys.any { it.startsWith("gp_") }
        val needYk = assetKeys.any { it.startsWith("yk_") }
        val needZr = assetKeys.any { it.startsWith("zr_") }

        val gold = if (needGold) fetchSivasGold() else emptyList()
        val yk = if (needYk) fetchYapiKrediRates() else emptyList()
        val zr = if (needZr) fetchZiraatRates() else emptyList()

        return LiveMarketSnapshot(
            goldPrices = gold,
            yapiKrediRates = yk,
            ziraatRates = zr
        )
    }

    private fun formatToTwoDecimals(value: String): String {
        val parsed = MarketPriceParser.parseDoubleOrNull(value) ?: return value
        return String.format(Locale("tr"), "%,.2f", parsed)
    }
}
