sed -i 's/containerColor = Color.White/containerColor = MaterialTheme.colorScheme.surface/g' app/src/main/java/com/example/ui/SavingsScreen.kt
sed -i 's/colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)/colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))/g' app/src/main/java/com/example/ui/SavingsScreen.kt
sed -i 's/containerColor = Color(0xFF0061A4)/containerColor = MaterialTheme.colorScheme.primary/g' app/src/main/java/com/example/ui/SavingsScreen.kt
sed -i 's/contentColor = Color.White/contentColor = MaterialTheme.colorScheme.onPrimary/g' app/src/main/java/com/example/ui/SavingsScreen.kt
sed -i 's/border = BorderStroke(1.dp, Color(0xFFF1F5F9))/border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))/g' app/src/main/java/com/example/ui/SavingsScreen.kt
sed -i 's/border = BorderStroke(1.dp, Color(0xFFECEFF3))/border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))/g' app/src/main/java/com/example/ui/SavingsScreen.kt
