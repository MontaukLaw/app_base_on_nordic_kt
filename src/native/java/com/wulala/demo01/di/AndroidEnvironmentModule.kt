package com.wulala.demo01.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.kotlin.ble.core.android.AndroidEnvironment
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment

// Nordic 内部用接口，告诉 Hilt：
// “AndroidEnvironment 就是 NativeAndroidEnvironment”
@Module
@InstallIn(SingletonComponent::class)
abstract class AndroidEnvironmentModule {

    @Binds
    abstract fun bindEnvironment(
        environment: NativeAndroidEnvironment
    ): AndroidEnvironment
}
