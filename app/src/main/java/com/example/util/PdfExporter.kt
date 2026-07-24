package com.example.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.Transaction
import com.example.data.TransactionType
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun exportToPdf(transactions: List<Transaction>, title: String, outputStream: OutputStream) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val incomePaint = Paint().apply {
            color = Color.parseColor("#16A34A") // Green
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val expensePaint = Paint().apply {
            color = Color.parseColor("#DC2626") // Red
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val savingPaint = Paint().apply {
            color = Color.parseColor("#0284C7") // Blue
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9") // Light Gray background for headers
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        var yPosition = 50f
        
        // Draw Title
        canvas.drawText("İŞLEM GEÇMİŞİ", pageInfo.pageWidth / 2f, yPosition, titlePaint)
        yPosition += 30f
        
        titlePaint.textSize = 14f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(title, pageInfo.pageWidth / 2f, yPosition, titlePaint)
        yPosition += 50f

        // Table Headers
        val margin = 50f
        val colDate = margin
        val colTitle = margin + 80f
        val colCategory = margin + 250f
        val colType = margin + 350f
        val colAmount = pageInfo.pageWidth - margin - 50f

        // Header Background
        canvas.drawRect(margin, yPosition - 15f, pageInfo.pageWidth - margin, yPosition + 10f, bgPaint)
        
        canvas.drawText("Tarih", colDate, yPosition, headerPaint)
        canvas.drawText("Başlık", colTitle, yPosition, headerPaint)
        canvas.drawText("Kategori", colCategory, yPosition, headerPaint)
        canvas.drawText("Tür", colType, yPosition, headerPaint)
        
        headerPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Tutar", pageInfo.pageWidth - margin - 10f, yPosition, headerPaint)
        headerPaint.textAlign = Paint.Align.LEFT
        
        yPosition += 30f

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))

        for (transaction in transactions) {
            // Check if we need a new page
            if (yPosition > pageInfo.pageHeight - 50f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            canvas.drawText(dateFormat.format(Date(transaction.timestamp)), colDate, yPosition, textPaint)
            
            // Truncate title if too long
            var displayTitle = if (transaction.person != null) "${transaction.title} (${transaction.person})" else transaction.title
            if (displayTitle.length > 25) {
                displayTitle = displayTitle.substring(0, 22) + "..."
            }
            canvas.drawText(displayTitle, colTitle, yPosition, textPaint)
            
            var displayCategory = transaction.category ?: "-"
            if (displayCategory.length > 15) {
                displayCategory = displayCategory.substring(0, 12) + "..."
            }
            canvas.drawText(displayCategory, colCategory, yPosition, textPaint)

            val typeStr = when (transaction.type) {
                TransactionType.INCOME -> "Gelir"
                TransactionType.EXPENSE -> "Gider"
                TransactionType.SAVING -> "Birikim"
            }
            canvas.drawText(typeStr, colType, yPosition, textPaint)

            val amountPaint = when (transaction.type) {
                TransactionType.INCOME -> incomePaint
                TransactionType.EXPENSE -> expensePaint
                TransactionType.SAVING -> savingPaint
            }
            
            amountPaint.textAlign = Paint.Align.RIGHT
            val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
            canvas.drawText(prefix + currencyFormat.format(transaction.amount), pageInfo.pageWidth - margin - 10f, yPosition, amountPaint)
            amountPaint.textAlign = Paint.Align.LEFT

            yPosition += 15f
            canvas.drawLine(margin, yPosition, pageInfo.pageWidth - margin, yPosition, linePaint)
            yPosition += 20f
        }

        // Draw Totals
        yPosition += 20f
        if (yPosition > pageInfo.pageHeight - 100f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
        }

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalSaving = transactions.filter { it.type == TransactionType.SAVING }.sumOf { it.amount }

        canvas.drawRect(margin, yPosition - 20f, pageInfo.pageWidth - margin, yPosition + 70f, bgPaint)
        
        val totalLabelPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        yPosition += 5f
        canvas.drawText("Toplam Gelir:", colType, yPosition, totalLabelPaint)
        incomePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("+" + currencyFormat.format(totalIncome), pageInfo.pageWidth - margin - 10f, yPosition, incomePaint)
        
        yPosition += 25f
        canvas.drawText("Toplam Gider:", colType, yPosition, totalLabelPaint)
        expensePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("-" + currencyFormat.format(totalExpense), pageInfo.pageWidth - margin - 10f, yPosition, expensePaint)
        
        yPosition += 25f
        canvas.drawText("Toplam Birikim:", colType, yPosition, totalLabelPaint)
        savingPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("-" + currencyFormat.format(totalSaving), pageInfo.pageWidth - margin - 10f, yPosition, savingPaint)

        pdfDocument.finishPage(page)

        try {
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
        }
    }
}
