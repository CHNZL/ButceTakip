sed -i 's/val uiState: StateFlow<BudgetUiState> = combine(/val uiState: StateFlow<BudgetUiState> = combine(\n        repository.besPortfolio,/g' app/src/main/java/com/example/viewmodel/BudgetViewModel.kt
sed -i 's/) { transactions, savings ->/) { bes, transactions, savings ->/g' app/src/main/java/com/example/viewmodel/BudgetViewModel.kt
sed -i 's/totalSaved = totalSaved/totalSaved = totalSaved,\n            besPortfolio = bes/g' app/src/main/java/com/example/viewmodel/BudgetViewModel.kt
