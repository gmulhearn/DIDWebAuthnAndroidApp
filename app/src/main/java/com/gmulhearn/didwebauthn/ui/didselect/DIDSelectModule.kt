package com.gmulhearn.didwebauthn.ui.didselect

import com.gmulhearn.didwebauthn.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * DIDSelect VIPER Dagger Module
 */
@Module
class DIDSelectModule: BaseFragmentModule<DIDSelectFragment>() {

    @Provides
    fun providePresenter(impl: DIDSelectPresenter): DIDSelectContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: DIDSelectInteractor): DIDSelectContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: DIDSelectRouter): DIDSelectContract.Router = impl
}
