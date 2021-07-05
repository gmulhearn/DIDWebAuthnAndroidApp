package com.gmulhearn.didwebauthn.ui.didcomm.contactselect

import com.gmulhearn.didwebauthn.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * ContactSelect VIPER Dagger Module
 */
@Module
class ContactSelectModule: BaseFragmentModule<ContactSelectFragment>() {

    @Provides
    fun providePresenter(impl: ContactSelectPresenter): ContactSelectContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: ContactSelectInteractor): ContactSelectContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: ContactSelectRouter): ContactSelectContract.Router = impl
}
