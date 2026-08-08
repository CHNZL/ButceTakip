package com.example.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class AmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val parts = originalText.split(",")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) "," + parts[1] else if (originalText.endsWith(",")) "," else ""

        val formattedInteger = if (integerPart.isNotEmpty()) {
            integerPart.reversed().chunked(3).joinToString(".").reversed()
        } else {
            ""
        }

        val formattedText = formattedInteger + decimalPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                
                val intLen = integerPart.length
                val isPastInteger = offset > intLen
                
                val originalIntOffset = if (isPastInteger) intLen else offset
                
                // For a reversed chunked formatting, the number of separators before originalIntOffset:
                // Let's count characters in integer part
                // Actually it's easier to map by iterating
                var transformedOffset = 0
                var originalCount = 0
                for (i in formattedInteger.indices) {
                    if (originalCount == originalIntOffset) break
                    if (formattedInteger[i] != '.') {
                        originalCount++
                    }
                    transformedOffset++
                }
                
                return if (isPastInteger) {
                    transformedOffset + (offset - intLen)
                } else {
                    transformedOffset
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val intLen = integerPart.length
                val formattedIntLen = formattedInteger.length
                
                val isPastInteger = offset > formattedIntLen
                
                var originalOffset = 0
                var transformedCount = 0
                for (i in formattedInteger.indices) {
                    if (transformedCount == minOf(offset, formattedIntLen)) break
                    if (formattedInteger[i] != '.') {
                        originalOffset++
                    }
                    transformedCount++
                }
                
                return if (isPastInteger) {
                    originalOffset + (offset - formattedIntLen)
                } else {
                    originalOffset
                }
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
