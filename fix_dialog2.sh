sed -i 's/quantityText.replace(".", "").replace(",", ".").toDoubleOrNull()/com.example.util.parseFormattedAmount(quantityText)/g' app/src/main/java/com/example/ui/AddTransactionDialog.kt
sed -i 's/unitPriceText.replace(".", "").replace(",", ".").toDoubleOrNull()/com.example.util.parseFormattedAmount(unitPriceText)/g' app/src/main/java/com/example/ui/AddTransactionDialog.kt
sed -i 's/amountText.replace(".", "").replace(",", ".").toDoubleOrNull()/com.example.util.parseFormattedAmount(amountText)/g' app/src/main/java/com/example/ui/AddTransactionDialog.kt
