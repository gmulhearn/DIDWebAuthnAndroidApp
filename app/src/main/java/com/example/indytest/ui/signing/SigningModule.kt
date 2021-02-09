package com.example.indytest.ui.signing

import com.example.indytest.common.di.BaseFragmentModule
import com.example.indytest.common.di.qualifier.DidInformation
import com.example.indytest.common.di.qualifier.WalletInformation
import dagger.Module
import dagger.Provides

/**
 * Signing VIPER Dagger Module
 */
@Module
class SigningModule: BaseFragmentModule<SigningFragment>() {

    @Provides
    fun providePresenter(impl: SigningPresenter): SigningContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: SigningInteractor): SigningContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: SigningRouter): SigningContract.Router = impl

    @Provides
    @DidInformation
    fun provideDidInfo(fragment: SigningFragment) = fragment.navigationArgs.didInfo

    @Provides
    @WalletInformation
    fun provideWalletInfo(fragment: SigningFragment) = fragment.navigationArgs.walletInfo
}
