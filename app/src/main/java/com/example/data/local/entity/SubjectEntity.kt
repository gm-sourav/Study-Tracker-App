package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: Long,
    val iconKey: String = "book",
    val weeklyGoalHours: Float = 5.0f,
    val createdAt: Long = System.currentTimeMillis()
)
