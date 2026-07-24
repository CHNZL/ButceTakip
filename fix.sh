sed -i '459,466c\
                val fileName = if (year == null) "Islem_Gecmisi_TumZamanlar.pdf" else "Islem_Gecmisi_${month?.plus(1)}_$year.pdf"\
                pdfFileNameToExport = fileName\
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {\
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)\
                }\
                pdfExportLauncher.launch(fileName)\
            }' app/src/main/java/com/example/ui/HistoryScreen.kt
