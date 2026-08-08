sed -i '/import com.example.data.SavingGoal/a import com.example.data.BesPortfolio' app/src/main/java/com/example/viewmodel/BudgetViewModel.kt
sed -i '/val totalSaved: Double = 0.0/a \    , val besPortfolio: BesPortfolio? = null' app/src/main/java/com/example/viewmodel/BudgetViewModel.kt

