sed -i '/if (editingCategoryPrice != null) {/i \    if (showBesDialog) { \
        BesDialog( \
            besPortfolio = besPortfolio, \
            onDismiss = { showBesDialog = false }, \
            onSave = { \
                onUpdateBes?.invoke(it) \
                showBesDialog = false \
            } \
        ) \
    }' app/src/main/java/com/example/ui/SavingsScreen.kt
