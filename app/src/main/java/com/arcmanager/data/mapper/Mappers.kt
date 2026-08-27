package com.arcmanager.data.mapper

import com.arcmanager.core.util.DateUtils
import com.arcmanager.data.remote.dto.*
import com.arcmanager.domain.model.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

// ── Client ──

fun ClientDto.toDomain(): Client {
    return Client(
        id = id ?: "",
        userId = userId ?: "",
        name = name,
        companyName = companyName,
        email = email,
        phone = phone,
        telegram = telegram,
        whatsapp = whatsapp,
        country = country,
        currency = currency ?: "INR",
        notes = notes,
        status = status ?: "active",
        tags = tags ?: emptyList(),
        createdAt = DateUtils.parseTimestamp(createdAt),
        updatedAt = DateUtils.parseTimestamp(updatedAt),
    )
}

fun Client.toDto(): ClientDto {
    return ClientDto(
        id = id.ifEmpty { null },
        userId = userId.ifEmpty { null },
        name = name,
        companyName = companyName,
        email = email,
        phone = phone,
        telegram = telegram,
        whatsapp = whatsapp,
        country = country,
        currency = currency,
        notes = notes,
        status = status,
        tags = tags.ifEmpty { null },
    )
}

// ── Project ──

fun ProjectDto.toDomain(): Project {
    return Project(
        id = id ?: "",
        userId = userId ?: "",
        clientId = clientId,
        name = name,
        description = description,
        totalAmount = BigDecimal.valueOf(totalAmount),
        currency = currency ?: "INR",
        paymentModel = PaymentModel.fromValue(paymentModel),
        status = status ?: "active",
        startDate = DateUtils.parseLocalDate(startDate),
        expectedCompletionDate = DateUtils.parseLocalDate(expectedCompletionDate),
        completedAt = DateUtils.parseTimestamp(completedAt),
        createdAt = DateUtils.parseTimestamp(createdAt),
        updatedAt = DateUtils.parseTimestamp(updatedAt),
    )
}

fun Project.toDto(): ProjectDto {
    return ProjectDto(
        id = id.ifEmpty { null },
        userId = userId.ifEmpty { null },
        clientId = clientId,
        name = name,
        description = description,
        totalAmount = totalAmount.toDouble(),
        currency = currency,
        paymentModel = paymentModel.value,
        status = status,
        startDate = startDate?.toString(),
        expectedCompletionDate = expectedCompletionDate?.toString(),
    )
}

// ── Payment ──

fun PaymentDto.toDomain(): Payment {
    return Payment(
        id = id ?: "",
        userId = userId ?: "",
        clientId = clientId,
        projectId = projectId,
        scheduleId = scheduleId,
        amount = BigDecimal.valueOf(amount),
        currency = currency ?: "INR",
        paymentDate = DateUtils.parseTimestamp(paymentDate) ?: Instant.now(),
        paymentType = PaymentType.fromValue(paymentType),
        paymentMethod = PaymentMethod.fromValue(paymentMethod),
        bankAccountId = bankAccountId,
        transactionReference = transactionReference,
        notes = notes,
        status = status ?: "received",
        createdAt = DateUtils.parseTimestamp(createdAt),
        updatedAt = DateUtils.parseTimestamp(updatedAt),
    )
}

fun Payment.toDto(): PaymentDto {
    return PaymentDto(
        id = id.ifEmpty { null },
        userId = userId.ifEmpty { null },
        clientId = clientId,
        projectId = projectId,
        scheduleId = scheduleId,
        amount = amount.toDouble(),
        currency = currency,
        paymentDate = paymentDate.toString(),
        paymentType = paymentType.value,
        paymentMethod = paymentMethod.value,
        bankAccountId = bankAccountId,
        transactionReference = transactionReference,
        notes = notes,
        status = status,
    )
}

// ── PaymentSchedule ──

fun PaymentScheduleDto.toDomain(): PaymentSchedule {
    return PaymentSchedule(
        id = id ?: "",
        userId = userId ?: "",
        projectId = projectId,
        clientId = clientId,
        title = title,
        amount = BigDecimal.valueOf(amount),
        currency = currency ?: "INR",
        dueDate = DateUtils.parseLocalDate(dueDate) ?: LocalDate.now(),
        paymentType = paymentType,
        status = status ?: "pending",
        recurrenceType = recurrenceType,
        recurrenceInterval = recurrenceInterval,
        parentScheduleId = parentScheduleId,
        createdAt = DateUtils.parseTimestamp(createdAt),
        updatedAt = DateUtils.parseTimestamp(updatedAt),
    )
}

fun PaymentSchedule.toDto(): PaymentScheduleDto {
    return PaymentScheduleDto(
        id = id.ifEmpty { null },
        userId = userId.ifEmpty { null },
        projectId = projectId,
        clientId = clientId,
        title = title,
        amount = amount.toDouble(),
        currency = currency,
        dueDate = dueDate.toString(),
        paymentType = paymentType,
        status = status,
        recurrenceType = recurrenceType,
        recurrenceInterval = recurrenceInterval,
        parentScheduleId = parentScheduleId?.ifEmpty { null },
    )
}

// ── BankAccount ──

fun BankAccountDto.toDomain(): BankAccount {
    return BankAccount(
        id = id ?: "",
        userId = userId ?: "",
        accountName = accountName,
        bankName = bankName,
        accountHolderName = accountHolderName,
        accountLast4 = accountLast4,
        currency = currency ?: "INR",
        isActive = isActive ?: true,
        createdAt = DateUtils.parseTimestamp(createdAt),
        updatedAt = DateUtils.parseTimestamp(updatedAt),
    )
}

// ── Profile ──

fun ProfileDto.toDomain(): User {
    return User(
        id = id ?: "",
        fullName = fullName,
        email = email,
        avatarUrl = avatarUrl,
        defaultCurrency = defaultCurrency ?: "INR",
        timezone = timezone ?: "Asia/Kolkata",
        createdAt = DateUtils.parseTimestamp(createdAt),
        updatedAt = DateUtils.parseTimestamp(updatedAt),
    )
}
