sed -i '/besPortfolio = uiState.besPortfolio,/d' app/src/main/java/com/example/ui/DashboardScreen.kt
sed -i '/onUpdateBes = { viewModel.updateBesPortfolio(it) }/d' app/src/main/java/com/example/ui/DashboardScreen.kt
