package com.rippleeffect.fleettracking.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rippleeffect.fleettracking.model.LocationRecord
import com.rippleeffect.fleettracking.repository.db.dao.LocationRecordsDao


/**
 * Database migration and config class
 */
@Database(
    entities = [LocationRecord::class],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationRecordsDao(): LocationRecordsDao

    companion object {
        fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "fleet_tracker.db"
            ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()


        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("ALTER TABLE locationrecord ADD COLUMN isLocationPermissionEnabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE locationrecord ADD COLUMN isActivityRecognitionPermissionEnabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE locationrecord ADD COLUMN origin INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE locationrecord ADD COLUMN isPowerSaverActivated INTEGER NOT NULL DEFAULT 0")
            }


        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE locationrecord ADD COLUMN isPowerSaverActivated INTEGER NOT NULL DEFAULT 0")
            }


        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE locationrecord ADD COLUMN provider TEXT NOT NULL DEFAULT ''")
            }


        }
    }
}