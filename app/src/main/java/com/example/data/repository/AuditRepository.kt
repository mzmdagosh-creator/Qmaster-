package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AuditLogEntity
import kotlinx.coroutines.flow.Flow

class AuditRepository(private val database: AppDatabase) {
    private val auditDao = database.auditDao()

    fun observeRecentLogs(): Flow<List<AuditLogEntity>> = auditDao.observeRecentLogs()
    fun countTotalLogs(): Flow<Int> = auditDao.countTotalLogs()
}
