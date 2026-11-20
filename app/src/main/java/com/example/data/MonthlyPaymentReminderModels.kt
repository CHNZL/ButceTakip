package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * User-configurable rule tracking whether a monthly expected expense item
 * has been entered for the current month.
 */
data class MonthlyExpectedPaymentRule(
    val id: String = UUID.randomUUID().toString(),
    val categoryName: String,
    val label: String = categoryName,
    val isEnabled: Boolean = true,
    val remindAfterDay: Int = 1,
    val defaultAmount: Double? = null,
    val defaultPerson: String? = null,
    val skippedForYearMonth: String? = null
) {
    val displayTitle: String
        get() = label.ifBlank { categoryName }

    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("categoryName", categoryName)
        put("label", label)
        put("isEnabled", isEnabled)
        put("remindAfterDay", remindAfterDay.coerceIn(1, 28))
        if (defaultAmount != null && defaultAmount > 0.0) {
            put("defaultAmount", defaultAmount)
        }
        if (!defaultPerson.isNullOrBlank()) {
            put("defaultPerson", defaultPerson)
        }
        if (!skippedForYearMonth.isNullOrBlank()) {
            put("skippedForYearMonth", skippedForYearMonth)
        }
    }

    companion object {
        fun listToJsonString(list: List<MonthlyExpectedPaymentRule>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJsonObject()) }
            return arr.toString()
        }

        fun listFromJsonString(raw: String?): List<MonthlyExpectedPaymentRule> {
            if (raw.isNullOrBlank()) return emptyList()
            return try {
                val arr = JSONArray(raw)
                val out = mutableListOf<MonthlyExpectedPaymentRule>()
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val cat = o.optString("categoryName").trim()
                    if (cat.isBlank()) continue
                    val lbl = o.optString("label", cat).trim().ifBlank { cat }
                    val amountRaw = if (o.has("defaultAmount") && !o.isNull("defaultAmount")) {
                        o.optDouble("defaultAmount", 0.0).takeIf { it > 0.0 }
                    } else null
                    val personRaw = if (o.has("defaultPerson") && !o.isNull("defaultPerson")) {
                        o.optString("defaultPerson").trim().ifBlank { null }
                    } else null
                    val skippedRaw = if (o.has("skippedForYearMonth") && !o.isNull("skippedForYearMonth")) {
                        o.optString("skippedForYearMonth").trim().ifBlank { null }
                    } else null

                    out.add(
                        MonthlyExpectedPaymentRule(
                            id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
                            categoryName = cat,
                            label = lbl,
                            isEnabled = o.optBoolean("isEnabled", true),
                            remindAfterDay = o.optInt("remindAfterDay", 1).coerceIn(1, 28),
                            defaultAmount = amountRaw,
                            defaultPerson = personRaw,
                            skippedForYearMonth = skippedRaw
                        )
                    )
                }
                out
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

enum class MonthlyRuleStatus {
    ENTERED_THIS_MONTH,
    MISSING_NOW,
    UPCOMING_THIS_MONTH,
    SKIPPED_THIS_MONTH,
    DISABLED
}

data class MonthlyRuleChecklistItem(
    val categoryName: String,
    val displayTitle: String,
    val rule: MonthlyExpectedPaymentRule?,
    val status: MonthlyRuleStatus,
    val enteredTransaction: Transaction? = null,
    val lastKnownAmount: Double? = null,
    val lastKnownPerson: String? = null,
    val pastMonthsSeen: Int = 0,
    val isCustomRule: Boolean = false
) {
    val isTracked: Boolean get() = rule?.isEnabled == true
    val remindAfterDay: Int get() = rule?.remindAfterDay ?: 1
    val suggestedAmount: Double? get() = rule?.defaultAmount ?: lastKnownAmount
    val suggestedPerson: String? get() = rule?.defaultPerson ?: lastKnownPerson
    val isRecommended: Boolean get() = !isTracked && pastMonthsSeen >= 2
}

object MonthlyPaymentEvaluator {
    private val trLocale = Locale("tr", "TR")

    fun normalizeKey(value: String): String = value.trim().lowercase(trLocale)

    fun currentYearMonthKey(now: Calendar = Calendar.getInstance()): String {
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun currentDateKey(now: Calendar = Calendar.getInstance()): String {
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1
        val day = now.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    private fun currentMonthBounds(now: Calendar): Pair<Long, Long> {
        val startCal = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    private fun yearMonthOf(timestamp: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    private fun matchesTransaction(
        tx: Transaction,
        categoryName: String,
        label: String? = null
    ): Boolean {
        if (tx.type != TransactionType.EXPENSE) return false
        val targetCat = normalizeKey(categoryName)
        val txCat = normalizeKey(tx.category)
        val txTitle = normalizeKey(tx.title)
        if (txCat == targetCat || txTitle == targetCat) return true
        if (!label.isNullOrBlank()) {
            val targetLabel = normalizeKey(label)
            if (txTitle == targetLabel || txCat == targetLabel) return true
        }
        return false
    }

    /**
     * Evaluates enabled monthly rules and returns only those that are due now
     * (today's day >= remindAfterDay), not skipped this month, and not yet entered this month.
     */
    fun getMissingDueNow(
        rules: List<MonthlyExpectedPaymentRule>,
        transactions: List<Transaction>,
        now: Calendar = Calendar.getInstance()
    ): List<MonthlyRuleChecklistItem> {
        val (monthStart, monthEnd) = currentMonthBounds(now)
        val todayDay = now.get(Calendar.DAY_OF_MONTH)
        val ymKey = currentYearMonthKey(now)

        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val thisMonthExpenses = expenses.filter { it.timestamp in monthStart until monthEnd }

        return rules
            .filter { it.isEnabled }
            .mapNotNull { rule ->
                if (rule.skippedForYearMonth == ymKey) return@mapNotNull null
                if (todayDay < rule.remindAfterDay) return@mapNotNull null

                val entered = thisMonthExpenses.firstOrNull {
                    matchesTransaction(it, rule.categoryName, rule.label)
                }
                if (entered != null) return@mapNotNull null

                val pastMatches = expenses
                    .filter { it.timestamp < monthStart && matchesTransaction(it, rule.categoryName, rule.label) }
                    .sortedByDescending { it.timestamp }

                val latestPast = pastMatches.firstOrNull()
                val distinctMonths = pastMatches.map { yearMonthOf(it.timestamp) }.distinct().size

                MonthlyRuleChecklistItem(
                    categoryName = rule.categoryName,
                    displayTitle = rule.displayTitle,
                    rule = rule,
                    status = MonthlyRuleStatus.MISSING_NOW,
                    enteredTransaction = null,
                    lastKnownAmount = latestPast?.amount,
                    lastKnownPerson = latestPast?.person,
                    pastMonthsSeen = distinctMonths
                )
            }
            .sortedWith(compareBy<MonthlyRuleChecklistItem> { it.remindAfterDay }.thenBy { it.displayTitle.lowercase(trLocale) })
    }

    /**
     * Builds the unified checklist combining all user-configured rules, Room expense categories,
     * and any historical expense categories so the user can toggle reminders per payment item.
     */
    fun buildUnifiedChecklist(
        rules: List<MonthlyExpectedPaymentRule>,
        expenseCategories: List<AppCategory>,
        transactions: List<Transaction>,
        now: Calendar = Calendar.getInstance()
    ): List<MonthlyRuleChecklistItem> {
        val (monthStart, monthEnd) = currentMonthBounds(now)
        val todayDay = now.get(Calendar.DAY_OF_MONTH)
        val ymKey = currentYearMonthKey(now)

        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val thisMonthExpenses = expenses.filter { it.timestamp in monthStart until monthEnd }

        val knownCategoryKeys = expenseCategories.map { normalizeKey(it.name) }.toSet()
        val rulesByKey = rules.associateBy { normalizeKey(it.categoryName) }

        val orderedKeys = LinkedHashMap<String, String>()
        // 1. All saved rules first (preserves custom items)
        rules.forEach { rule ->
            val key = normalizeKey(rule.categoryName)
            if (key.isNotBlank() && !orderedKeys.containsKey(key)) {
                orderedKeys[key] = rule.categoryName
            }
        }
        // 2. All Room expense categories
        expenseCategories.forEach { cat ->
            val key = normalizeKey(cat.name)
            if (key.isNotBlank() && !orderedKeys.containsKey(key)) {
                orderedKeys[key] = cat.name
            }
        }
        // 3. Any extra categories present in past expenses
        expenses.forEach { tx ->
            val key = normalizeKey(tx.category)
            if (key.isNotBlank() && !orderedKeys.containsKey(key)) {
                orderedKeys[key] = tx.category
            }
        }

        return orderedKeys.map { (key, catName) ->
            val rule = rulesByKey[key]
            val label = rule?.displayTitle ?: catName
            val entered = thisMonthExpenses.firstOrNull { matchesTransaction(it, catName, rule?.label) }
            val pastMatches = expenses
                .filter { it.timestamp < monthStart && matchesTransaction(it, catName, rule?.label) }
                .sortedByDescending { it.timestamp }
            val latestPast = pastMatches.firstOrNull()
            val distinctMonths = pastMatches.map { yearMonthOf(it.timestamp) }.distinct().size

            val status = when {
                rule == null || !rule.isEnabled -> MonthlyRuleStatus.DISABLED
                entered != null -> MonthlyRuleStatus.ENTERED_THIS_MONTH
                rule.skippedForYearMonth == ymKey -> MonthlyRuleStatus.SKIPPED_THIS_MONTH
                todayDay >= rule.remindAfterDay -> MonthlyRuleStatus.MISSING_NOW
                else -> MonthlyRuleStatus.UPCOMING_THIS_MONTH
            }

            MonthlyRuleChecklistItem(
                categoryName = catName,
                displayTitle = label,
                rule = rule,
                status = status,
                enteredTransaction = entered,
                lastKnownAmount = latestPast?.amount,
                lastKnownPerson = latestPast?.person,
                pastMonthsSeen = distinctMonths,
                isCustomRule = rule != null && key !in knownCategoryKeys
            )
        }.sortedWith(
            compareBy<MonthlyRuleChecklistItem> { item ->
                when (item.status) {
                    MonthlyRuleStatus.MISSING_NOW -> 0
                    MonthlyRuleStatus.UPCOMING_THIS_MONTH -> 1
                    MonthlyRuleStatus.ENTERED_THIS_MONTH -> 2
                    MonthlyRuleStatus.SKIPPED_THIS_MONTH -> 3
                    MonthlyRuleStatus.DISABLED -> if (item.isRecommended) 4 else 5
                }
            }.thenBy { it.remindAfterDay }
                .thenBy { it.displayTitle.lowercase(trLocale) }
        )
    }
}
