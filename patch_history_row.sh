sed -i '197,215c\
        Row(\
            modifier = Modifier.fillMaxWidth(),\
            horizontalArrangement = Arrangement.SpaceBetween,\
            verticalAlignment = Alignment.CenterVertically\
        ) {\
            Column {\
                Text("İşlem Geçmişi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)\
                Text("TÜM GELİR VE GİDER KAYITLARI", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)\
            }\
            Button(\
                onClick = { showPdfExportDialog = true },\
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),\
                shape = RoundedCornerShape(8.dp),\
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)\
            ) {\
                Text("PDF", fontWeight = FontWeight.ExtraBold)\
            }\
        }\
' app/src/main/java/com/example/ui/HistoryScreen.kt
