package com.gmulhearn.didwebauthn.ui.home

import com.gmulhearn.didwebauthn.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * Home VIPER Dagger Module
 */
@Module
class HomeModule: BaseFragmentModule<HomeFragment>() {

    @Provides
    fun providePresenter(impl: HomePresenter): HomeContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: HomeInteractor): HomeContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: HomeRouter): HomeContract.Router = impl
}
