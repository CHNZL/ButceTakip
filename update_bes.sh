sed -i 's/val stateContributionReturn: Double/val stateContributionReturn: Double,\n    val isRetired: Boolean = false/g' app/src/main/java/com/example/data/BesPortfolio.kt
sed -i 's/`stateContributionReturn` REAL NOT NULL/`stateContributionReturn` REAL NOT NULL, `isRetired` INTEGER NOT NULL DEFAULT 0/g' app/src/main/java/com/example/data/AppDatabase.kt
