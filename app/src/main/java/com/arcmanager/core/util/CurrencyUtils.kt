package com.arcmanager.core.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {

    /**
     * Format a BigDecimal amount with the appropriate currency symbol.
     * Examples: "₹1,24,500", "$10,000.00"
     */
    fun formatAmount(amount: BigDecimal, currencyCode: String = "INR"): String {
        val symbol = Constants.CURRENCY_SYMBOLS[currencyCode] ?: currencyCode
        val formatted = when (currencyCode) {
            "INR" -> formatIndian(amount)
            "JPY" -> formatNoDecimals(amount)
            else -> formatInternational(amount)
        }
        return "$symbol$formatted"
    }

    /**
     * Format using Indian numbering system (12,34,567).
     */
    private fun formatIndian(amount: BigDecimal): String {
        val isNegative = amount < BigDecimal.ZERO
        val abs = amount.abs()
        val intPart = abs.toBigInteger().toString()
        val decPart = abs.remainder(BigDecimal.ONE)
            .movePointRight(2)
            .toBigInteger()
            .toString()
            .padStart(2, '0')

        val formatted = if (intPart.length <= 3) {
            intPart
        } else {
            val last3 = intPart.takeLast(3)
            val remaining = intPart.dropLast(3)
            val groups = mutableListOf<String>()
            var i = remaining.length
            while (i > 0) {
                val start = maxOf(0, i - 2)
                groups.add(0, remaining.substring(start, i))
                i = start
            }
            groups.joinToString(",") + ",$last3"
        }

        val result = if (decPart == "00") formatted else "$formatted.$decPart"
        return if (isNegative) "-$result" else result
    }

    /**
     * Standard international formatting with 2 decimal places.
     */
    private fun formatInternational(amount: BigDecimal): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(amount)
    }

    /**
     * Format without decimals (for JPY, etc.)
     */
    private fun formatNoDecimals(amount: BigDecimal): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    /**
     * Compact formatting for charts/cards: ₹1.2L, $10K, etc.
     */
    fun formatCompact(amount: BigDecimal, currencyCode: String = "INR"): String {
        val symbol = Constants.CURRENCY_SYMBOLS[currencyCode] ?: currencyCode
        val abs = amount.abs()
        val formatted = when {
            currencyCode == "INR" && abs >= BigDecimal(10_000_000) ->
                "${abs.divide(BigDecimal(10_000_000), 1, java.math.RoundingMode.HALF_UP)}Cr"
            currencyCode == "INR" && abs >= BigDecimal(100_000) ->
                "${abs.divide(BigDecimal(100_000), 1, java.math.RoundingMode.HALF_UP)}L"
            abs >= BigDecimal(1_000_000) ->
                "${abs.divide(BigDecimal(1_000_000), 1, java.math.RoundingMode.HALF_UP)}M"
            abs >= BigDecimal(1_000) ->
                "${abs.divide(BigDecimal(1_000), 1, java.math.RoundingMode.HALF_UP)}K"
            else -> abs.toPlainString()
        }
        val prefix = if (amount < BigDecimal.ZERO) "-" else ""
        return "$prefix$symbol$formatted"
    }
}
