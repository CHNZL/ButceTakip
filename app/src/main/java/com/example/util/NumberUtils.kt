package com.example.util

fun formatInputAmount(text: String): String {
    var result = ""
    var hasDecimal = false
    
    for (char in text) {
        if (char.isDigit()) {
            result += char
        } else if ((char == ',' || char == '.') && !hasDecimal) {
            result += ','
            hasDecimal = true
        }
    }
    
    val parts = result.split(",")
    if (parts.size > 1) {
        result = parts[0] + "," + parts[1].take(2)
    }
    
    return result
}

fun parseFormattedAmount(text: String): Double {
    return text.replace(" ", "").replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
}

fun formatDoubleForInput(value: Double): String {
    if (value == 0.0) return ""
    val str = java.math.BigDecimal(value.toString()).toPlainString()
    val cleanStr = if (str.endsWith(".0")) str.dropLast(2) else str
    return cleanStr.replace(".", ",")
}
