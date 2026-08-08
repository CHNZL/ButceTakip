sed -i 's/text = "%${String.format(Locale("tr"), "%.1f", abs(data.diffPercent))}",/text = "%${com.example.util.FormatUtil.getNumberFormat(1).format(abs(data.diffPercent))}",/g' app/src/main/java/com/example/ui/TrendAnalysisScreen.kt

sed -i 's/text = "Toplam: %,.2f ₺".format(Locale("tr"), computedAmount),/text = "Toplam: ${com.example.util.FormatUtil.getCurrencyFormat().format(computedAmount)}",/g' app/src/main/java/com/example/ui/AddTransactionDialog.kt
