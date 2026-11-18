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
import com.anchorguard.client.data.local.dao.SyncStatusDao
import com.anchorguard.client.data.local.dao.UsageEventDao
import com.anchorguard.client.data.local.entity.AppPolicy
import com.anchorguard.client.data.local.entity.DeviceSetting
import com.anchorguard.client.data.local.entity.DeviceUser
import com.anchorguard.client.data.local.entity.SyncStatus
import com.anchorguard.client.data.local.entity.UsageEvent

@Database(
    entities = [
        DeviceUser::class,
        AppPolicy::class,
        DeviceSetting::class,
        UsageEvent::class,
        SyncStatus::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AnchorGuardDatabase : RoomDatabase() {
    abstract fun deviceUserDao(): DeviceUserDao
    abstract fun appPolicyDao(): AppPolicyDao
    abstract fun deviceSettingDao(): DeviceSettingDao
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
                // Reserved for future schema evolution.
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
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)

            if (BuildConfig.DEBUG) {
                builder.fallbackToDestructiveMigration()
            }

            return builder.build()
        }
    }
}
