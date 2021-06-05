package com.example.did.ui.externalauth

import com.example.did.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * ExternalAuth VIPER Dagger Module
 */
@Module
class ExternalAuthModule: BaseFragmentModule<ExternalAuthFragment>() {

    @Provides
    fun providePresenter(impl: ExternalAuthPresenter): ExternalAuthContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: ExternalAuthInteractor): ExternalAuthContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: ExternalAuthRouter): ExternalAuthContract.Router = impl

}
