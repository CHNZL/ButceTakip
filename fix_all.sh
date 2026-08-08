#!/bin/bash

# Fix AddTransactionDialog.kt
sed -i 's/modifier = Modifier.weight(1f).height(50.dp)/visualTransformation = com.example.util.AmountVisualTransformation(), modifier = Modifier.weight(1f).height(50.dp)/g' app/src/main/java/com/example/ui/AddTransactionDialog.kt
sed -i 's/modifier = Modifier.fillMaxWidth().height(50.dp)/visualTransformation = com.example.util.AmountVisualTransformation(), modifier = Modifier.fillMaxWidth().height(50.dp)/g' app/src/main/java/com/example/ui/AddTransactionDialog.kt

# Fix SavingsScreen.kt
sed -i 's/modifier = Modifier.fillMaxWidth().testTag("custom_price_input")/visualTransformation = com.example.util.AmountVisualTransformation(), modifier = Modifier.fillMaxWidth().testTag("custom_price_input")/g' app/src/main/java/com/example/ui/SavingsScreen.kt
