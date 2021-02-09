package com.example.indytest.Signing

import com.example.indytest.common.di.BaseFragmentModule
import com.example.indytest.common.di.qualifier.DidInfo
import com.example.indytest.common.di.qualifier.WalletInfo
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
    @DidInfo
    fun provideDidInfo(fragment: SigningFragment) = fragment.navigationArgs.didInfo

    @Provides
    @WalletInfo
    fun provideWalletInfo(fragment: SigningFragment) = fragment.navigationArgs.walletInfo
}
