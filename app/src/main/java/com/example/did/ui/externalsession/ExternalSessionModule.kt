package com.example.did.ui.externalsession

import com.example.did.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * ExternalSession VIPER Dagger Module
 */
@Module
class ExternalSessionModule: BaseFragmentModule<ExternalSessionFragment>() {

    @Provides
    fun providePresenter(impl: ExternalSessionPresenter): ExternalSessionContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: ExternalSessionInteractor): ExternalSessionContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: ExternalSessionRouter): ExternalSessionContract.Router = impl

}
