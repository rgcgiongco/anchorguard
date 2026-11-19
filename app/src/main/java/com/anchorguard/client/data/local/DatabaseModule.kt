package com.anchorguard.client.data.local

import android.content.Context
import com.anchorguard.client.data.local.dao.AppPolicyDao
import com.anchorguard.client.data.local.dao.DeviceSettingDao
import com.anchorguard.client.data.local.dao.DeviceUserDao
import com.anchorguard.client.data.local.dao.LocationDao
import com.anchorguard.client.data.local.dao.SyncStatusDao
import com.anchorguard.client.data.local.dao.UsageEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AnchorGuardDatabase =
        AnchorGuardDatabase.getInstance(context)

    @Provides
    fun provideDeviceUserDao(db: AnchorGuardDatabase): DeviceUserDao = db.deviceUserDao()

    @Provides
    fun provideAppPolicyDao(db: AnchorGuardDatabase): AppPolicyDao = db.appPolicyDao()

    @Provides
    fun provideDeviceSettingDao(db: AnchorGuardDatabase): DeviceSettingDao = db.deviceSettingDao()

    @Provides
    fun provideLocationDao(db: AnchorGuardDatabase): LocationDao = db.locationDao()

    @Provides
    fun provideUsageEventDao(db: AnchorGuardDatabase): UsageEventDao = db.usageEventDao()

    @Provides
    fun provideSyncStatusDao(db: AnchorGuardDatabase): SyncStatusDao = db.syncStatusDao()
}
