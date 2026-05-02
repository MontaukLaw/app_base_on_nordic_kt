package com.wulala.demo01.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.native
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment
import javax.inject.Singleton

// 👉 整个 App 只有一个 CentralManager
// 👉 所有 ViewModel / Repository 共用
@Module // 依赖的配方集合
@InstallIn(SingletonComponent::class)      // SingletonComponent 的意义是：给全 App 用的（Application 级别
object BleModule {                                 // object是常见的kotlin单例对象

    @Provides   // “这是一个工厂方法，Hilt 用它来创建对象”。
    @Singleton  // “在该容器里只创建一次，并缓存复用”。
    fun provideAppScope(): CoroutineScope {  // 函数签名：返回一个 CoroutineScope
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides  // 这是创建 CentralManager 的方法
    @Singleton // CentralManager 全 App 只创建一次（同一个实例）
    fun provideCentralManager(
        environment: NativeAndroidEnvironment,  // 先想办法拿到 NativeAndroidEnvironment, 这个由EnvironmentModule提供
        scope: CoroutineScope                   // 再想办法拿到 CoroutineScope
    ): CentralManager {                         // 然后调用这个方法，把它们传进来，得到 CentralManager
        return CentralManager.native(environment, scope)   // CentralManager的工厂方法生成
    }
}
