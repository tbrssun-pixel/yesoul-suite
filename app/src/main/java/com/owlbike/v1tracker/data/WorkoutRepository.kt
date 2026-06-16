package com.owlbike.v1tracker.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val dao: WorkoutDao) {
    fun observeSessions(): Flow<List<WorkoutSessionEntity>> = dao.observeSessions()

    fun observeSamples(sessionId: String): Flow<List<WorkoutSampleEntity>> = dao.observeSamples(sessionId)

    suspend fun samplesForSession(sessionId: String): List<WorkoutSampleEntity> = dao.samplesForSession(sessionId)

    fun observeLatestDiagnostic(): Flow<DiagnosticSnapshotEntity?> = dao.observeLatestDiagnostic()

    fun observeRememberedDevices(): Flow<List<RememberedDeviceEntity>> = dao.observeRememberedDevices()

    suspend fun createSession(session: WorkoutSessionEntity) = dao.insertSession(session)

    suspend fun saveSample(sample: WorkoutSampleEntity) = dao.insertSample(sample)

    suspend fun saveDiagnostic(snapshot: DiagnosticSnapshotEntity) = dao.insertDiagnostic(snapshot)

    suspend fun saveRememberedDevice(device: RememberedDeviceEntity) = dao.upsertRememberedDevice(device)

    suspend fun deleteRememberedDevice(address: String) = dao.deleteRememberedDevice(address)

    suspend fun deleteSession(sessionId: String) = dao.deleteSession(sessionId)

    suspend fun finishSession(session: WorkoutSessionEntity, endTimeMillis: Long): WorkoutSessionEntity {
        val sessionId = session.id
        val updated = session.copy(
            endTimeMillis = endTimeMillis,
            state = "finished",
            sampleCount = dao.countSamples(sessionId),
            averagePowerWatts = dao.averagePower(sessionId),
            averageCadenceRpm = dao.averageCadence(sessionId),
            averageHeartRateBpm = dao.averageHeartRate(sessionId),
        )
        dao.updateSession(updated)
        return updated
    }

    suspend fun updateSession(session: WorkoutSessionEntity) = dao.updateSession(session)
}
