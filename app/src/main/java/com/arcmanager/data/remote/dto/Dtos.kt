package com.arcmanager.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    @SerialName("company_name") val companyName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val telegram: String? = null,
    val whatsapp: String? = null,
    val country: String? = null,
    val currency: String? = "INR",
    val notes: String? = null,
    val status: String? = "active",
    val tags: List<String>? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ProjectDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("client_id") val clientId: String,
    val name: String,
    val description: String? = null,
    @SerialName("total_amount") val totalAmount: Double,
    val currency: String? = "INR",
    @SerialName("payment_model") val paymentModel: String,
    val status: String? = "active",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("expected_completion_date") val expectedCompletionDate: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class PaymentDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("client_id") val clientId: String,
    @SerialName("project_id") val projectId: String? = null,
    @SerialName("schedule_id") val scheduleId: String? = null,
    val amount: Double,
    val currency: String? = "INR",
    @SerialName("payment_date") val paymentDate: String,
    @SerialName("payment_type") val paymentType: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("bank_account_id") val bankAccountId: String? = null,
    @SerialName("transaction_reference") val transactionReference: String? = null,
    val notes: String? = null,
    val status: String? = "received",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class PaymentScheduleDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("project_id") val projectId: String,
    @SerialName("client_id") val clientId: String,
    val title: String? = null,
    val amount: Double,
    val currency: String? = "INR",
    @SerialName("due_date") val dueDate: String,
    @SerialName("payment_type") val paymentType: String? = null,
    val status: String? = "pending",
    @SerialName("recurrence_type") val recurrenceType: String? = null,
    @SerialName("recurrence_interval") val recurrenceInterval: Int? = null,
    @SerialName("parent_schedule_id") val parentScheduleId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class BankAccountDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("account_name") val accountName: String,
    @SerialName("bank_name") val bankName: String,
    @SerialName("account_holder_name") val accountHolderName: String? = null,
    @SerialName("encrypted_account_number") val encryptedAccountNumber: String? = null,
    @SerialName("account_last4") val accountLast4: String? = null,
    @SerialName("encrypted_ifsc") val encryptedIfsc: String? = null,
    val currency: String? = "INR",
    @SerialName("is_active") val isActive: Boolean? = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ProfileDto(
    val id: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("default_currency") val defaultCurrency: String? = "INR",
    val timezone: String? = "Asia/Kolkata",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class AuditLogDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("entity_type") val entityType: String? = null,
    @SerialName("entity_id") val entityId: String? = null,
    val action: String? = null,
    @SerialName("old_data") val oldData: String? = null,
    @SerialName("new_data") val newData: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
