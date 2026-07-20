package com.example.util

fun formatInputAmount(text: String): String {
    // replace dot with comma for users who might press dot on keypad
    var cleanedText = text.replace(".", ",")
    
    val firstCommaIndex = cleanedText.indexOf(',')
    if (firstCommaIndex != -1) {
        cleanedText = cleanedText.substring(0, firstCommaIndex + 1) + 
                      cleanedText.substring(firstCommaIndex + 1).replace(",", "")
    }

    cleanedText = cleanedText.filter { it.isDigit() || it == ',' }

    if (cleanedText.isEmpty()) return ""

    val parts = cleanedText.split(",")
    val integerPart = parts[0]
    
    val formattedInteger = if (integerPart.isNotEmpty()) {
        integerPart.reversed().chunked(3).joinToString(".").reversed()
    } else {
        if (parts.size > 1) "0" else ""
    }

    val decimalPart = if (parts.size > 1) {
        "," + parts[1].take(2)
    } else if (cleanedText.endsWith(",")) {
        ","
    } else {
        ""
    }

    return formattedInteger + decimalPart
}

fun parseFormattedAmount(text: String): Double {
    return text.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
}
