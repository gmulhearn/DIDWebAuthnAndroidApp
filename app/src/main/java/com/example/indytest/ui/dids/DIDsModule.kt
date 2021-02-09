package com.example.indytest.ui.dids

import com.example.indytest.common.di.BaseFragmentModule
import com.example.indytest.common.di.qualifier.WalletInformation
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
    @WalletInformation
    fun provideDidInfo(fragment: DIDsFragment) = fragment.navigationArgs.walletInfo
}
