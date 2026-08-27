package com.arcmanager.domain.repository

import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, fullName: String): Result<User>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun isLoggedIn(): Boolean
    suspend fun updateProfile(user: User): Result<User>
}
