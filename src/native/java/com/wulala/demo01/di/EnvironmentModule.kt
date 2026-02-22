package com.wulala.demo01.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment
import javax.inject.Singleton

// BLE 的“空气 / 土壤 / 操作系统接口”，全 App 仅此一份
@Module
@InstallIn(SingletonComponent::class)
object EnvironmentModule {

    @Provides
    @Singleton
    fun provideEnvironment(
        @ApplicationContext context: Context
    ): NativeAndroidEnvironment {
        // App 级唯一 Environment
        // 不再绑定 Activity / ViewModel 生命周期
        return NativeAndroidEnvironment.getInstance(
            context,
            isNeverForLocationFlagSet = true
        )
    }
}
