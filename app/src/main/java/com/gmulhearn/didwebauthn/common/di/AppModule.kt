package com.gmulhearn.didwebauthn.common.di

import android.app.Application
import android.content.Context
import com.gmulhearn.didwebauthn.common.DefaultDispatcherProvider
import com.gmulhearn.didwebauthn.common.DefaultWalletProvider
import com.gmulhearn.didwebauthn.common.DispatcherProvider
import com.gmulhearn.didwebauthn.common.WalletProvider
import com.gmulhearn.didwebauthn.transport.relay.FirebaseRelayRepository
import com.gmulhearn.didwebauthn.transport.relay.RelayRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        FragmentBindingModule::class
    ]
)
class AppModule {

    @Provides
    @Singleton
    fun provideApplication(context: Context): Application {
        return context.applicationContext as Application
    }

    @Provides
    @Singleton
    fun provideDispatchersProvider(impl: DefaultDispatcherProvider): DispatcherProvider = impl

    @Provides
    @Singleton
    fun provideWallet(impl: DefaultWalletProvider): WalletProvider = impl

    @Provides
    @Singleton
    fun provideRelayService(impl: FirebaseRelayRepository): RelayRepository = impl
}
