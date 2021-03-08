package com.example.did.ui.signing

import com.example.did.common.di.BaseFragmentModule
import com.example.did.common.di.qualifier.DidInformation
import com.example.did.common.di.qualifier.WalletInformation
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
}
