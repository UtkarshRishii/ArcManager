package com.arcmanager.domain.model

import java.time.Instant

data class User(
    val id: String,
    val fullName: String?,
    val email: String?,
    val avatarUrl: String?,
    val defaultCurrency: String = "INR",
    val timezone: String = "Asia/Kolkata",
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)
