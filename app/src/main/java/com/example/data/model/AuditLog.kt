package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["timestamp"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userRole: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
