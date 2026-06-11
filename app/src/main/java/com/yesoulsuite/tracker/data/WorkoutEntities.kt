package com.yesoulsuite.tracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val state: String,
    val deviceName: String?,
    val deviceAddress: String?,
    val totalDistanceMeters: Double?,
    val totalCalories: Int?,
    val averagePowerWatts: Double?,
    val averageCadenceRpm: Double?,
    val averageHeartRateBpm: Double?,
    val sampleCount: Int,
)

@Entity(
    tableName = "workout_samples",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index("timestampMillis")],
)
data class WorkoutSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestampMillis: Long,
    val elapsedSeconds: Long,
    val speedKmh: Double?,
    val cadenceRpm: Double?,
    val powerWatts: Int?,
    val heartRateBpm: Int?,
    val resistanceLevel: Double?,
    val distanceMeters: Double?,
    val calories: Int?,
)

@Entity(tableName = "diagnostic_snapshots")
data class DiagnosticSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAtMillis: Long,
    val deviceName: String?,
    val deviceAddress: String?,
    val servicesText: String,
)
