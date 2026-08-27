package com.arcmanager.core.util

object Constants {

    // Payment Models
    const val PAYMENT_MODEL_ONE_TIME = "ONE_TIME"
    const val PAYMENT_MODEL_ADVANCE_FINAL = "ADVANCE_FINAL"
    const val PAYMENT_MODEL_INSTALLMENTS = "INSTALLMENTS"
    const val PAYMENT_MODEL_MONTHLY_RECURRING = "MONTHLY_RECURRING"
    const val PAYMENT_MODEL_CUSTOM = "CUSTOM"

    // Payment Types
    const val PAYMENT_TYPE_ADVANCE = "ADVANCE"
    const val PAYMENT_TYPE_MILESTONE = "MILESTONE"
    const val PAYMENT_TYPE_INSTALLMENT = "INSTALLMENT"
    const val PAYMENT_TYPE_FINAL = "FINAL"
    const val PAYMENT_TYPE_MONTHLY = "MONTHLY"
    const val PAYMENT_TYPE_OTHER = "OTHER"

    // Payment Methods
    const val METHOD_BANK_TRANSFER = "BANK_TRANSFER"
    const val METHOD_UPI = "UPI"
    const val METHOD_CASH = "CASH"
    const val METHOD_CARD = "CARD"
    const val METHOD_PAYPAL = "PAYPAL"
    const val METHOD_WISE = "WISE"
    const val METHOD_SWIFT = "SWIFT"
    const val METHOD_OTHER = "OTHER"

    // Payment Status
    const val STATUS_RECEIVED = "received"
    const val STATUS_PENDING = "pending"
    const val STATUS_FAILED = "failed"
    const val STATUS_REFUNDED = "refunded"

    // Schedule Status
    const val SCHEDULE_PENDING = "pending"
    const val SCHEDULE_PARTIALLY_PAID = "partially_paid"
    const val SCHEDULE_PAID = "paid"
    const val SCHEDULE_OVERDUE = "overdue"
    const val SCHEDULE_CANCELLED = "cancelled"

    // Client Status
    const val CLIENT_ACTIVE = "active"
    const val CLIENT_ARCHIVED = "archived"

    // Project Status
    const val PROJECT_ACTIVE = "active"
    const val PROJECT_COMPLETED = "completed"
    const val PROJECT_ON_HOLD = "on_hold"
    const val PROJECT_CANCELLED = "cancelled"

    // Recurrence Types
    const val RECURRENCE_WEEKLY = "WEEKLY"
    const val RECURRENCE_MONTHLY = "MONTHLY"
    const val RECURRENCE_QUARTERLY = "QUARTERLY"
    const val RECURRENCE_YEARLY = "YEARLY"
    const val RECURRENCE_CUSTOM = "CUSTOM"

    // Supported Currencies
    val SUPPORTED_CURRENCIES = listOf(
        "INR", "USD", "EUR", "GBP", "SGD", "NZD", "AUD", "CAD", "JPY", "AED"
    )

    // Currency Symbols
    val CURRENCY_SYMBOLS = mapOf(
        "INR" to "₹",
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "SGD" to "S$",
        "NZD" to "NZ$",
        "AUD" to "A$",
        "CAD" to "C$",
        "JPY" to "¥",
        "AED" to "د.إ"
    )

    // Supabase Table Names
    const val TABLE_PROFILES = "profiles"
    const val TABLE_CLIENTS = "clients"
    const val TABLE_PROJECTS = "projects"
    const val TABLE_PAYMENT_SCHEDULES = "payment_schedules"
    const val TABLE_PAYMENTS = "payments"
    const val TABLE_BANK_ACCOUNTS = "bank_accounts"
    const val TABLE_ATTACHMENTS = "attachments"
    const val TABLE_NOTIFICATIONS = "notifications"
    const val TABLE_AUDIT_LOGS = "audit_logs"

    // Storage Buckets
    const val BUCKET_ATTACHMENTS = "attachments"
}
