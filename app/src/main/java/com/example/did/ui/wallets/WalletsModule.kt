package com.example.did.ui.wallets

import com.example.did.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * Wallets VIPER Dagger Module
 */
@Module
class WalletsModule: BaseFragmentModule<WalletsFragment>() {

    @Provides
    fun providePresenter(impl: WalletsPresenter): WalletsContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: WalletsInteractor): WalletsContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: WalletsRouter): WalletsContract.Router = impl

}
