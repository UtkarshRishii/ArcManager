package com.arcmanager.core.util

import java.math.BigDecimal

object ValidationUtils {

    fun validateClientName(name: String): String? {
        return when {
            name.isBlank() -> "Client name is required"
            name.length < 2 -> "Client name must be at least 2 characters"
            name.length > 200 -> "Client name is too long"
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return null // Email is optional
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return if (!emailRegex.matches(email)) "Invalid email address" else null
    }

    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return null // Phone is optional
        val cleaned = phone.replace(Regex("[\\s\\-()]+"), "")
        return when {
            cleaned.length < 7 -> "Phone number is too short"
            cleaned.length > 15 -> "Phone number is too long"
            !cleaned.matches(Regex("^\\+?[0-9]+$")) -> "Invalid phone number"
            else -> null
        }
    }

    fun validateAmount(amountStr: String): String? {
        if (amountStr.isBlank()) return "Amount is required"
        val amount = amountStr.replace(",", "").toBigDecimalOrNull()
        return when {
            amount == null -> "Invalid amount"
            amount <= BigDecimal.ZERO -> "Amount must be greater than zero"
            amount > BigDecimal("99999999999999.99") -> "Amount is too large"
            else -> null
        }
    }

    fun validateProjectName(name: String): String? {
        return when {
            name.isBlank() -> "Project name is required"
            name.length < 2 -> "Project name must be at least 2 characters"
            name.length > 300 -> "Project name is too long"
            else -> null
        }
    }

    fun validateBankAccountName(name: String): String? {
        return when {
            name.isBlank() -> "Account name is required"
            name.length > 100 -> "Account name is too long"
            else -> null
        }
    }

    fun validateBankName(name: String): String? {
        return when {
            name.isBlank() -> "Bank name is required"
            name.length > 100 -> "Bank name is too long"
            else -> null
        }
    }

    fun validateAccountNumber(number: String): String? {
        if (number.isBlank()) return null // Optional
        val cleaned = number.replace(Regex("[\\s\\-]+"), "")
        return when {
            cleaned.length < 4 -> "Account number is too short"
            cleaned.length > 20 -> "Account number is too long"
            !cleaned.matches(Regex("^[0-9]+$")) -> "Account number must contain only digits"
            else -> null
        }
    }

    fun validateIFSC(ifsc: String): String? {
        if (ifsc.isBlank()) return null // Optional
        return if (!ifsc.matches(Regex("^[A-Z]{4}0[A-Z0-9]{6}$"))) {
            "Invalid IFSC code"
        } else null
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun validateLoginEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !email.contains("@") -> "Invalid email address"
            else -> null
        }
    }

    /**
     * Parse an amount string to BigDecimal, removing commas and spaces.
     */
    fun parseAmount(amountStr: String): BigDecimal? {
        return amountStr.replace(",", "").replace(" ", "").toBigDecimalOrNull()
    }
}
