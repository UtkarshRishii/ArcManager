package com.arcmanager.data.repository

import com.arcmanager.core.di.IoDispatcher
import com.arcmanager.core.util.Constants
import com.arcmanager.core.util.Result
import com.arcmanager.data.mapper.toDomain
import com.arcmanager.data.mapper.toDto
import com.arcmanager.data.remote.dto.ProjectDto
import com.arcmanager.domain.model.Project
import com.arcmanager.domain.repository.ProjectRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ProjectRepository {

    private val userId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("Not authenticated")

    override fun getProjectsByClient(clientId: String): Flow<Result<List<Project>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_PROJECTS]
                .select {
                    filter { eq("user_id", userId); eq("client_id", clientId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
            val projects = response.decodeList<ProjectDto>().map { it.toDomain() }
            emit(Result.success(projects))
        } catch (e: Exception) {
            emit(Result.error("Failed to load projects: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override fun getAllProjects(status: String?): Flow<Result<List<Project>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_PROJECTS]
                .select {
                    filter {
                        eq("user_id", userId)
                        if (status != null) eq("status", status)
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
            val projects = response.decodeList<ProjectDto>().map { it.toDomain() }
            emit(Result.success(projects))
        } catch (e: Exception) {
            emit(Result.error("Failed to load projects: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override suspend fun getProjectById(projectId: String): Result<Project> {
        return try {
            val dto = supabase.postgrest[Constants.TABLE_PROJECTS]
                .select { filter { eq("id", projectId); eq("user_id", userId) } }
                .decodeSingle<ProjectDto>()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to load project: ${e.message}", e)
        }
    }

    override suspend fun createProject(project: Project): Result<Project> {
        return try {
            val dto = project.copy(userId = userId).toDto()
            val response = supabase.postgrest[Constants.TABLE_PROJECTS]
                .insert(dto) { select() }
                .decodeSingle<ProjectDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to create project: ${e.message}", e)
        }
    }

    override suspend fun updateProject(project: Project): Result<Project> {
        return try {
            val dto = project.toDto()
            supabase.postgrest[Constants.TABLE_PROJECTS]
                .update(dto) { filter { eq("id", project.id); eq("user_id", userId) } }
            Result.success(project)
        } catch (e: Exception) {
            Result.error("Failed to update project: ${e.message}", e)
        }
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            supabase.postgrest[Constants.TABLE_PROJECTS]
                .delete { filter { eq("id", projectId); eq("user_id", userId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to delete project: ${e.message}", e)
        }
    }
}
