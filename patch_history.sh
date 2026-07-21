sed -i 's/fun HistoryScreen(transactions: List<Transaction>, onEdit: ((Transaction) -> Unit)? = null, onDelete: ((Transaction) -> Unit)? = null)/fun HistoryScreen(transactions: List<Transaction>, onEdit: ((Transaction) -> Unit)? = null, onDelete: ((Transaction) -> Unit)? = null, isDark: Boolean = false)/g' app/src/main/java/com/example/ui/HistoryScreen.kt

sed -i 's/HistoryScreen(/HistoryScreen(isDark = isDark,/g' app/src/main/java/com/example/ui/DashboardScreen.kt

