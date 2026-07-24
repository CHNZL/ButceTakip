sed -i '65a\
    var pdfFileNameToExport by remember { mutableStateOf("") }\
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }\
' app/src/main/java/com/example/ui/HistoryScreen.kt

sed -i 's/Toast.makeText(context, "PDF başarıyla kaydedildi.", Toast.LENGTH_LONG).show()/Toast.makeText(context, "PDF başarıyla kaydedildi.", Toast.LENGTH_LONG).show()\n                        showPdfNotification(context, uri, pdfFileNameToExport)/g' app/src/main/java/com/example/ui/HistoryScreen.kt

