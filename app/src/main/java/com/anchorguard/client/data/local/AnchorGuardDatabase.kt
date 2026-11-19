package com.anchorguard.client.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.anchorguard.client.BuildConfig
import com.anchorguard.client.data.local.dao.AppPolicyDao
import com.anchorguard.client.data.local.dao.DeviceSettingDao
import com.anchorguard.client.data.local.dao.DeviceUserDao
import com.anchorguard.client.data.local.dao.LocationDao
import com.anchorguard.client.data.local.dao.SyncStatusDao
import com.anchorguard.client.data.local.dao.UsageEventDao
import com.anchorguard.client.data.local.entity.AppPolicy
import com.anchorguard.client.data.local.entity.DeviceSetting
import com.anchorguard.client.data.local.entity.DeviceUser
import com.anchorguard.client.data.local.entity.Location
import com.anchorguard.client.data.local.entity.SyncStatus
import com.anchorguard.client.data.local.entity.UsageEvent

@Database(
    entities = [
        DeviceUser::class,
        AppPolicy::class,
        DeviceSetting::class,
        Location::class,
        UsageEvent::class,
        SyncStatus::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AnchorGuardDatabase : RoomDatabase() {
    abstract fun deviceUserDao(): DeviceUserDao
    abstract fun appPolicyDao(): AppPolicyDao
    abstract fun deviceSettingDao(): DeviceSettingDao
    abstract fun locationDao(): LocationDao
    abstract fun usageEventDao(): UsageEventDao
    abstract fun syncStatusDao(): SyncStatusDao

    companion object {
        const val DATABASE_NAME = "anchorguard.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usage_events ADD COLUMN retryCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE usage_events ADD COLUMN deadLetter INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_usage_events_deadLetter ON usage_events(deadLetter)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS locations (" +
                    "id TEXT NOT NULL PRIMARY KEY," +
                    "device_id TEXT NOT NULL," +
                    "latitude REAL NOT NULL," +
                    "longitude REAL NOT NULL," +
                    "accuracy_meters INTEGER NOT NULL," +
                    "timestamp_millis INTEGER NOT NULL," +
                    "provider TEXT NOT NULL," +
                    "CREATE INDEX IF NOT EXISTS index_locations_device_id ON locations(device_id)," +
                    "CREATE INDEX IF NOT EXISTS index_locations_timestamp_millis ON locations(timestamp_millis)," +
                    "CREATE INDEX IF NOT EXISTS index_locations_device_timestamp ON locations(device_id, timestamp_millis)" +
                    ")"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE locations ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE locations ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_locations_synced ON locations(synced)")
            }
        }

        @Volatile
        private var INSTANCE: AnchorGuardDatabase? = null

        fun getInstance(context: Context): AnchorGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }
        }

        private fun build(context: Context): AnchorGuardDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AnchorGuardDatabase::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            if (BuildConfig.DEBUG) {
                builder.fallbackToDestructiveMigration()
            }

            return builder.build()
        }
    }
}
