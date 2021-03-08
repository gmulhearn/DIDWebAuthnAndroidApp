package com.example.did.common.di

import android.app.Application
import android.content.Context
import com.example.did.common.DefaultDispatcherProvider
import com.example.did.common.DefaultWalletProvider
import com.example.did.common.DispatcherProvider
import com.example.did.common.WalletProvider
import com.example.did.common.di.qualifier.WalletObject
import dagger.Module
import dagger.Provides
import org.hyperledger.indy.sdk.wallet.Wallet
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
}
