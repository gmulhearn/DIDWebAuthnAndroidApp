package com.gmulhearn.didwebauthn.ui.retired.signing

import com.gmulhearn.didwebauthn.common.di.BaseFragmentModule
import com.gmulhearn.didwebauthn.common.di.qualifier.DidInformation
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
