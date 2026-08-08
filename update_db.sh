sed -i 's/version = 5/version = 6/g' app/src/main/java/com/example/data/AppDatabase.kt
sed -i 's/entities = \[Transaction::class, SavingGoal::class, AppCategory::class, Person::class\]/entities = \[Transaction::class, SavingGoal::class, AppCategory::class, Person::class, BesPortfolio::class\]/g' app/src/main/java/com/example/data/AppDatabase.kt
