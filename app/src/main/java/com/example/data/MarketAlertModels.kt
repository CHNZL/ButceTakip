package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

/**
 * Configuration for periodic live market summary notifications.
 */
data class MarketScheduleConfig(
    val enabled: Boolean = false,
    val startTime: String = "09:00",
    val endTime: String = "18:00",
    val intervalMinutes: Int = 60,
    val weekdaysOnly: Boolean = false,
    val selectedAssetKeys: Set<String> = setOf(
        "gp_24 AYAR GRAM ALTIN",
        "yk_USD",
        "yk_EUR"
    )
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("enabled", enabled)
        obj.put("startTime", startTime)
        obj.put("endTime", endTime)
        obj.put("intervalMinutes", intervalMinutes)
        obj.put("weekdaysOnly", weekdaysOnly)
        val arr = JSONArray()
        selectedAssetKeys.forEach { arr.put(it) }
        obj.put("selectedAssetKeys", arr)
        return obj.toString()
    }

    companion object {
        fun fromJsonString(raw: String?): MarketScheduleConfig {
            if (raw.isNullOrBlank()) return MarketScheduleConfig()
            return try {
                val obj = JSONObject(raw)
                val keys = mutableSetOf<String>()
                val arr = obj.optJSONArray("selectedAssetKeys")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val s = arr.optString(i)
                        if (s.isNotBlank()) keys.add(s)
                    }
                }
                MarketScheduleConfig(
                    enabled = obj.optBoolean("enabled", false),
                    startTime = obj.optString("startTime", "09:00").ifBlank { "09:00" },
                    endTime = obj.optString("endTime", "18:00").ifBlank { "18:00" },
                    intervalMinutes = obj.optInt("intervalMinutes", 60).coerceAtLeast(15),
                    weekdaysOnly = obj.optBoolean("weekdaysOnly", false),
                    selectedAssetKeys = keys.ifEmpty {
                        setOf("gp_24 AYAR GRAM ALTIN", "yk_USD", "yk_EUR")
                    }
                )
            } catch (_: Exception) {
                MarketScheduleConfig()
            }
        }
    }
}

/**
 * Individual price threshold alarm for a live market instrument.
 */
data class MarketPriceAlarm(
    val id: String = UUID.randomUUID().toString(),
    val assetKey: String,
    val assetLabel: String,
    val priceField: String = FIELD_SELL,
    val direction: String = DIR_ABOVE,
    val targetPrice: Double,
    val isEnabled: Boolean = true,
    val oneTime: Boolean = true,
    val lastTriggeredAt: Long = 0L
) {
    val isAbove: Boolean get() = direction == DIR_ABOVE
    val isSell: Boolean get() = priceField == FIELD_SELL

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("assetKey", assetKey)
        put("assetLabel", assetLabel)
        put("priceField", priceField)
        put("direction", direction)
        put("targetPrice", targetPrice)
        put("isEnabled", isEnabled)
        put("oneTime", oneTime)
        put("lastTriggeredAt", lastTriggeredAt)
    }

    companion object {
        const val FIELD_SELL = "SELL"
        const val FIELD_BUY = "BUY"
        const val DIR_ABOVE = "ABOVE"
        const val DIR_BELOW = "BELOW"

        fun listToJsonString(list: List<MarketPriceAlarm>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJsonObject()) }
            return arr.toString()
        }

        fun listFromJsonString(raw: String?): List<MarketPriceAlarm> {
            if (raw.isNullOrBlank()) return emptyList()
            return try {
                val arr = JSONArray(raw)
                val out = mutableListOf<MarketPriceAlarm>()
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val key = o.optString("assetKey")
                    val target = o.optDouble("targetPrice", 0.0)
                    if (key.isBlank() || target <= 0.0) continue
                    out.add(
                        MarketPriceAlarm(
                            id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                            assetKey = key,
                            assetLabel = o.optString("assetLabel").ifBlank { key },
                            priceField = o.optString("priceField", FIELD_SELL),
                            direction = o.optString("direction", DIR_ABOVE),
                            targetPrice = target,
                            isEnabled = o.optBoolean("isEnabled", true),
                            oneTime = o.optBoolean("oneTime", true),
                            lastTriggeredAt = o.optLong("lastTriggeredAt", 0L)
                        )
                    )
                }
                out
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

/**
 * Unified descriptor for selectable market assets across Sivas Gold, Yapı Kredi, and Ziraat.
 */
data class MarketAssetOption(
    val key: String,
    val label: String,
    val shortLabel: String,
    val sourceLabel: String,
    val buyText: String,
    val sellText: String,
    val buyValue: Double?,
    val sellValue: Double?
)

object MarketPriceParser {
    /**
     * Parses Turkish/European or US formatted numeric strings (e.g., "6.750,50", "34,29", "6400")
     * safely into a Double.
     */
    fun parseDoubleOrNull(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw.trim().replace("₺", "").replace("TL", "").replace(" ", "")
        if (cleaned == "-" || cleaned.isEmpty()) return null
        val normalized = when {
            cleaned.contains(".") && cleaned.contains(",") -> {
                // Assume '.' is thousands separator and ',' is decimal (e.g. "10.600,50")
                if (cleaned.lastIndexOf(',') > cleaned.lastIndexOf('.')) {
                    cleaned.replace(".", "").replace(",", ".")
                } else {
                    cleaned.replace(",", "")
                }
            }
            cleaned.contains(",") -> cleaned.replace(",", ".")
            cleaned.count { it == '.' } > 1 -> cleaned.replace(".", "")
            cleaned.contains(".") -> {
                val afterDot = cleaned.substringAfterLast('.')
                if (afterDot.length == 3) cleaned.replace(".", "") else cleaned
            }
            else -> cleaned
        }
        val value = normalized.toDoubleOrNull() ?: return null
        return if (value > 0.0) value else null
    }

    fun formatPrice(value: Double): String {
        val locale = Locale("tr", "TR")
        return if (value >= 1000.0) {
            String.format(locale, "%,.0f ₺", value)
        } else {
            String.format(locale, "%,.2f ₺", value)
        }
    }

    fun buildCatalog(
        goldPrices: List<GoldPrice>,
        bankRates: List<BankRate>,
        ziraatRates: List<BankRate>
    ): List<MarketAssetOption> {
        val result = mutableListOf<MarketAssetOption>()

        val defaultGoldNames = listOf(
            "24 AYAR GRAM ALTIN" to "24A Gram",
            "22 AYAR GRAM ALTIN" to "22A Gram",
            "22 AYAR BİLEZİK" to "22A Bilezik",
            "ÇEYREK (YENİ)" to "Çeyrek Yeni",
            "ÇEYREK (ESKİ)" to "Çeyrek Eski",
            "14 AYAR GRAM ALTIN" to "14A Gram"
        )
        if (goldPrices.isNotEmpty()) {
            goldPrices.forEach { gp ->
                val shortName = defaultGoldNames.firstOrNull { it.first.equals(gp.name, ignoreCase = true) }?.second ?: gp.name
                result.add(
                    MarketAssetOption(
                        key = "gp_${gp.name}",
                        label = "$shortName (Sivas)",
                        shortLabel = shortName,
                        sourceLabel = "Altın (Sivas)",
                        buyText = gp.buy,
                        sellText = gp.sell,
                        buyValue = parseDoubleOrNull(gp.buy),
                        sellValue = parseDoubleOrNull(gp.sell)
                    )
                )
            }
        } else {
            defaultGoldNames.take(4).forEach { (full, shortName) ->
                result.add(
                    MarketAssetOption(
                        key = "gp_$full",
                        label = "$shortName (Sivas)",
                        shortLabel = shortName,
                        sourceLabel = "Altın (Sivas)",
                        buyText = "-",
                        sellText = "-",
                        buyValue = null,
                        sellValue = null
                    )
                )
            }
        }

        val ykDefaults = listOf(
            "USD" to "Dolar",
            "EUR" to "Euro",
            "XAU" to "Gram Altın",
            "XAG" to "Gram Gümüş"
        )
        if (bankRates.isNotEmpty()) {
            bankRates.forEach { br ->
                val shortName = ykDefaults.firstOrNull { it.first.equals(br.code, ignoreCase = true) }?.first ?: br.code
                result.add(
                    MarketAssetOption(
                        key = "yk_${br.code}",
                        label = "${br.code} (Yapı Kredi)",
                        shortLabel = "$shortName YK",
                        sourceLabel = "Yapı Kredi",
                        buyText = br.buy,
                        sellText = br.sell,
                        buyValue = parseDoubleOrNull(br.buy),
                        sellValue = parseDoubleOrNull(br.sell)
                    )
                )
            }
        } else {
            ykDefaults.forEach { (code, _) ->
                result.add(
                    MarketAssetOption(
                        key = "yk_$code",
                        label = "$code (Yapı Kredi)",
                        shortLabel = "$code YK",
                        sourceLabel = "Yapı Kredi",
                        buyText = "-",
                        sellText = "-",
                        buyValue = null,
                        sellValue = null
                    )
                )
            }
        }

        if (ziraatRates.isNotEmpty()) {
            ziraatRates.forEach { zr ->
                result.add(
                    MarketAssetOption(
                        key = "zr_${zr.code}",
                        label = "${zr.code} (Ziraat)",
                        shortLabel = "${zr.code} Ziraat",
                        sourceLabel = "Ziraat",
                        buyText = zr.buy,
                        sellText = zr.sell,
                        buyValue = parseDoubleOrNull(zr.buy),
                        sellValue = parseDoubleOrNull(zr.sell)
                    )
                )
            }
        } else {
            ykDefaults.forEach { (code, _) ->
                result.add(
                    MarketAssetOption(
                        key = "zr_$code",
                        label = "$code (Ziraat)",
                        shortLabel = "$code Ziraat",
                        sourceLabel = "Ziraat",
                        buyText = "-",
                        sellText = "-",
                        buyValue = null,
                        sellValue = null
                    )
                )
            }
        }

        return result
    }
}
