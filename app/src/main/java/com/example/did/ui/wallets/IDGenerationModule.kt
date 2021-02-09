package com.example.did.ui.wallets

import com.example.did.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * IDGeneration VIPER Dagger Module
 */
@Module
class IDGenerationModule: BaseFragmentModule<IDGenerationFragment>() {

    @Provides
    fun providePresenter(impl: IDGenerationPresenter): IDGenerationContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: IDGenerationInteractor): IDGenerationContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: IDGenerationRouter): IDGenerationContract.Router = impl

}
