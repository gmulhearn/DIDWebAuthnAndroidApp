package com.example.did.ui.walletInfo

import com.example.did.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * WalletInfo VIPER Dagger Module
 */
@Module
class WalletInfoModule: BaseFragmentModule<WalletInfoFragment>() {

    @Provides
    fun providePresenter(impl: WalletInfoPresenter): WalletInfoContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: WalletInfoInteractor): WalletInfoContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: WalletInfoRouter): WalletInfoContract.Router = impl

}
