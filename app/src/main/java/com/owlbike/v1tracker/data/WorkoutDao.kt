package com.owlbike.v1tracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startTimeMillis DESC")
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_samples WHERE sessionId = :sessionId ORDER BY timestampMillis ASC")
    fun observeSamples(sessionId: String): Flow<List<WorkoutSampleEntity>>

    @Query("SELECT * FROM workout_samples WHERE sessionId = :sessionId ORDER BY timestampMillis ASC")
    suspend fun samplesForSession(sessionId: String): List<WorkoutSampleEntity>

    @Query("SELECT * FROM diagnostic_snapshots ORDER BY createdAtMillis DESC LIMIT 1")
    fun observeLatestDiagnostic(): Flow<DiagnosticSnapshotEntity?>

    @Query("SELECT * FROM remembered_devices ORDER BY lastConnectedMillis DESC")
    fun observeRememberedDevices(): Flow<List<RememberedDeviceEntity>>

    @Query("SELECT COUNT(*) FROM workout_samples WHERE sessionId = :sessionId")
    suspend fun countSamples(sessionId: String): Int

    @Query("SELECT AVG(powerWatts) FROM workout_samples WHERE sessionId = :sessionId AND powerWatts IS NOT NULL")
    suspend fun averagePower(sessionId: String): Double?

    @Query("SELECT AVG(cadenceRpm) FROM workout_samples WHERE sessionId = :sessionId AND cadenceRpm IS NOT NULL")
    suspend fun averageCadence(sessionId: String): Double?

    @Query("SELECT AVG(heartRateBpm) FROM workout_samples WHERE sessionId = :sessionId AND heartRateBpm IS NOT NULL")
    suspend fun averageHeartRate(sessionId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Insert
    suspend fun insertSample(sample: WorkoutSampleEntity)

    @Insert
    suspend fun insertDiagnostic(snapshot: DiagnosticSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRememberedDevice(device: RememberedDeviceEntity)

    @Query("DELETE FROM remembered_devices WHERE address = :address")
    suspend fun deleteRememberedDevice(address: String)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
