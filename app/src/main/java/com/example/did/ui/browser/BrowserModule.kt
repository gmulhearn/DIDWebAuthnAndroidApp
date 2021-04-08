package com.example.did.ui.browser

import com.example.did.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * Browser VIPER Dagger Module
 */
@Module
class BrowserModule: BaseFragmentModule<BrowserFragment>() {

    @Provides
    fun providePresenter(impl: BrowserPresenter): BrowserContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: BrowserInteractor): BrowserContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: BrowserRouter): BrowserContract.Router = impl

}
