package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

object FormatUtil {
    private val symbols = DecimalFormatSymbols(Locale("tr", "TR")).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }

    fun getCurrencyFormat(): NumberFormat {
        return DecimalFormat("#,##0.00 ₺", symbols)
    }

    fun getCurrencyFormatNoDecimals(): NumberFormat {
        return DecimalFormat("#,##0 ₺", symbols)
    }

    fun getNumberFormat(decimals: Int = 2): NumberFormat {
        return when (decimals) {
            0 -> DecimalFormat("#,##0", symbols)
            1 -> DecimalFormat("#,##0.0", symbols)
            else -> DecimalFormat("#,##0.00", symbols)
        }
    }
}
