sed -i '411a\
    if (showPdfExportDialog) {\
        PdfExportDialog(\
            onDismiss = { showPdfExportDialog = false },\
            onExport = { year, month ->\
                showPdfExportDialog = false\
                val filtered = if (year == null || month == null) {\
                    pdfTitleToExport = "Tüm Zamanlar"\
                    transactions\
                } else {\
                    pdfTitleToExport = "${month + 1}.$year"\
                    transactions.filter { tx ->\
                        val cal = Calendar.getInstance()\
                        cal.timeInMillis = tx.timestamp\
                        cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month\
                    }\
                }\
                pdfTransactionsToExport = filtered.sortedByDescending { it.timestamp }\
                val fileName = if (year == null) "Islem_Gecmisi_TumZamanlar.pdf" else "Islem_Gecmisi_${month?.plus(1)}_$year.pdf"\
                pdfExportLauncher.launch(fileName)\
            }\
        )\
    }\
' app/src/main/java/com/example/ui/HistoryScreen.kt
