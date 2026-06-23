package com.owlbike.v1tracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WorkoutSessionEntity::class,
        WorkoutSampleEntity::class,
        DiagnosticSnapshotEntity::class,
        RememberedDeviceEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile private var instance: WorkoutDatabase? = null

        fun get(context: Context): WorkoutDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "owl-bike-workouts.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN goalType TEXT")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN goalSource TEXT")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN goalTargetDistanceMeters REAL")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN goalTargetCalories INTEGER")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN baselineMedianDistanceMeters REAL")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN baselineMedianCalories INTEGER")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN baselineMedianDurationSeconds INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS remembered_devices (
                        address TEXT NOT NULL PRIMARY KEY,
                        name TEXT,
                        lastConnectedMillis INTEGER NOT NULL,
                        lastRssi INTEGER,
                        serviceUuidsText TEXT
                    )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN goalTargetDurationSeconds INTEGER")
            }
        }
    }
}
