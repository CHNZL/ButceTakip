sed -i '/var newPriceText by remember/a \    var showBesDialog by remember { mutableStateOf(false) }' app/src/main/java/com/example/ui/SavingsScreen.kt
sed -i 's/\/\* Create BES \*\//showBesDialog = true/g' app/src/main/java/com/example/ui/SavingsScreen.kt
sed -i 's/\/\/ onEdit/showBesDialog = true/g' app/src/main/java/com/example/ui/SavingsScreen.kt
