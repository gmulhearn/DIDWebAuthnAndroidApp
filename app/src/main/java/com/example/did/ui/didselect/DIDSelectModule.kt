package com.example.did.ui.didselect

import com.example.did.common.di.BaseFragmentModule
import com.example.did.common.di.qualifier.WalletInformation
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
