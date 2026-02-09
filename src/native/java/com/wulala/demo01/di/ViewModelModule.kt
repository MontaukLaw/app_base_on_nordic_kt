package com.wulala.demo01.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.ViewModelLifecycle
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import no.nordicsemi.kotlin.ble.advertiser.android.BluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.advertiser.android.native
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.native
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @ViewModelScoped
    @Provides
    fun provideViewModelCoroutineScope(lifecycle: ViewModelLifecycle): CoroutineScope {
        return CoroutineScope(SupervisorJob())
            // Cancel the scope when the ViewModel is cleared.
            .also { scope ->
                lifecycle.addOnClearedListener {
                    println("AAA Cancelling scope!")
                    scope.cancel()
                }
            }
    }

    @ViewModelScoped
    @Provides
    fun providesAdvertiser(environment: NativeAndroidEnvironment): BluetoothLeAdvertiser {
        return BluetoothLeAdvertiser.native(environment)
    }

    @ViewModelScoped
    @Provides
    fun provideCentralManager(environment: NativeAndroidEnvironment, scope: CoroutineScope): CentralManager {
        return CentralManager.native(environment, scope)
    }
}