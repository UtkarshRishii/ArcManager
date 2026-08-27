package com.arcmanager.domain.repository

import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getProjectsByClient(clientId: String): Flow<Result<List<Project>>>
    fun getAllProjects(status: String? = null): Flow<Result<List<Project>>>
    suspend fun getProjectById(projectId: String): Result<Project>
    suspend fun createProject(project: Project): Result<Project>
    suspend fun updateProject(project: Project): Result<Project>
    suspend fun deleteProject(projectId: String): Result<Unit>
}
