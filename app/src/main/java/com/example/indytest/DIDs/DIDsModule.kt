package com.example.indytest.DIDs

import com.example.indytest.common.di.BaseFragmentModule
import com.example.indytest.common.di.qualifier.WalletInfo
import dagger.Module
import dagger.Provides

/**
 * DIDs VIPER Dagger Module
 */
@Module
class DIDsModule: BaseFragmentModule<DIDsFragment>() {

    @Provides
    fun providePresenter(impl: DIDsPresenter): DIDsContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: DIDsInteractor): DIDsContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: DIDsRouter): DIDsContract.Router = impl

    @Provides
    @WalletInfo
    fun provideDidInfo(fragment: DIDsFragment) = fragment.navigationArgs.walletInfo
}
