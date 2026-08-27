package com.arcmanager.data.repository

import com.arcmanager.core.di.IoDispatcher
import com.arcmanager.core.util.Result
import com.arcmanager.data.mapper.toDomain
import com.arcmanager.data.remote.dto.ProfileDto
import com.arcmanager.domain.model.User
import com.arcmanager.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> =
        withContext(dispatcher) {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                getCurrentUser()
            } catch (e: Exception) {
                Result.error("Login failed: ${e.message}", e)
            }
        }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String
    ): Result<User> = withContext(dispatcher) {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext Result.error("Registration failed")
            // Create profile
            val profile = ProfileDto(
                id = userId,
                fullName = fullName,
                email = email,
            )
            supabase.postgrest["profiles"].insert(profile)
            getCurrentUser()
        } catch (e: Exception) {
            Result.error("Registration failed: ${e.message}", e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> =
        withContext(dispatcher) {
            try {
                supabase.auth.resetPasswordForEmail(email)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.error("Failed to send reset email: ${e.message}", e)
            }
        }

    override suspend fun logout(): Result<Unit> = withContext(dispatcher) {
        try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Logout failed: ${e.message}", e)
        }
    }

    override suspend fun getCurrentUser(): Result<User?> = withContext(dispatcher) {
        try {
            val authUser = supabase.auth.currentUserOrNull()
                ?: return@withContext Result.success(null)
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", authUser.id) } }
                .decodeSingleOrNull<ProfileDto>()
            if (profile != null) {
                Result.success(profile.toDomain())
            } else {
                Result.success(
                    User(
                        id = authUser.id,
                        fullName = null,
                        email = authUser.email,
                    )
                )
            }
        } catch (e: Exception) {
            Result.error("Failed to get user: ${e.message}", e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return try {
            supabase.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateProfile(user: User): Result<User> =
        withContext(dispatcher) {
            try {
                val profile = ProfileDto(
                    id = user.id,
                    fullName = user.fullName,
                    email = user.email,
                    avatarUrl = user.avatarUrl,
                    defaultCurrency = user.defaultCurrency,
                    timezone = user.timezone,
                )
                supabase.postgrest["profiles"]
                    .update(profile) { filter { eq("id", user.id) } }
                Result.success(user)
            } catch (e: Exception) {
                Result.error("Failed to update profile: ${e.message}", e)
            }
        }
}
