sed -i '/val totalCurrentValue = /i \
    val besTotalValue = remember(besPortfolio) { \
        besPortfolio?.let { \
            val years = (System.currentTimeMillis() - it.startDate) / (1000L * 60 * 60 * 24 * 365) \
            val stateVesting = if (it.isRetired) 1.0 else { \
                when { \
                    years < 3 -> 0.0 \
                    years < 6 -> 0.15 \
                    years < 10 -> 0.35 \
                    else -> 0.60 \
                } \
            } \
            it.investment + it.investmentReturn + (it.stateContribution + it.stateContributionReturn) * stateVesting \
        } ?: 0.0 \
    } \
    val besPaid = besPortfolio?.investment ?: 0.0' app/src/main/java/com/example/ui/SavingsScreen.kt

sed -i 's/val totalCurrentValue = assetSummaries.sumOf { it.currentValue }/val totalCurrentValue = assetSummaries.sumOf { it.currentValue } + besTotalValue/g' app/src/main/java/com/example/ui/SavingsScreen.kt
sed -i 's/val totalPaidAll = assetSummaries.sumOf { it.totalPaid }/val totalPaidAll = assetSummaries.sumOf { it.totalPaid } + besPaid/g' app/src/main/java/com/example/ui/SavingsScreen.kt
