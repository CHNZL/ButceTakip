sed -i 's/text.replace(".", "").replace(",", ".")/com.example.util.parseFormattedAmount(text).toString()/g' app/src/main/java/com/example/ui/MortgageCalculatorScreen.kt
sed -i 's/s.replace(".", "").replace(",", ".").trim().toDoubleOrNull()/com.example.util.parseFormattedAmount(s)/g' app/src/main/java/com/example/ui/SavingsScreen.kt
