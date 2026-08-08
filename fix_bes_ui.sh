sed -i '/\/\/ --- 3. LEDGER HISTORY HEADER & BUTTON ---/i \        item { \
            if (besPortfolio != null) { \
                Spacer(modifier = Modifier.height(8.dp)) \
                BesSummaryCard(besPortfolio = besPortfolio, besTotalValue = besTotalValue, besPaid = besPaid, currencyFormat = currencyFormat) { \
                    // onEdit \
                } \
                Spacer(modifier = Modifier.height(16.dp)) \
            } else { \
                Spacer(modifier = Modifier.height(16.dp)) \
                Button(onClick = { /* Create BES */ }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { \
                    Text("Bireysel Emeklilik (BES) Ekle") \
                } \
                Spacer(modifier = Modifier.height(16.dp)) \
            } \
        }' app/src/main/java/com/example/ui/SavingsScreen.kt
