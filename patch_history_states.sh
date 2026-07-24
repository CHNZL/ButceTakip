sed -i '/var expandedTypeFilter/a \
    var showPdfExportDialog by remember { mutableStateOf(false) }\
    var pdfTransactionsToExport by remember { mutableStateOf<List<Transaction>>(emptyList()) }\
    var pdfTitleToExport by remember { mutableStateOf("") }\
    val context = LocalContext.current\
    val coroutineScope = rememberCoroutineScope()\
    val pdfExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->\
        if (uri != null) {\
            coroutineScope.launch(Dispatchers.IO) {\
                try {\
                    context.contentResolver.openOutputStream(uri)?.use { out ->\
                        PdfExporter.exportToPdf(pdfTransactionsToExport, pdfTitleToExport, out)\
                    }\
                    withContext(Dispatchers.Main) {\
                        Toast.makeText(context, "PDF başarıyla kaydedildi.", Toast.LENGTH_LONG).show()\
                    }\
                } catch (e: Exception) {\
                    withContext(Dispatchers.Main) {\
                        Toast.makeText(context, "PDF kaydedilirken hata oluştu.", Toast.LENGTH_LONG).show()\
                    }\
                }\
            }\
        }\
    }\
' app/src/main/java/com/example/ui/HistoryScreen.kt
