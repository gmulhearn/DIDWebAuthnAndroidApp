package com.gmulhearn.didwebauthn.ui.didcomm.AddContact

import com.gmulhearn.didwebauthn.common.di.BaseFragmentModule
import com.gmulhearn.didwebauthn.common.di.qualifier.DidInformation
import com.gmulhearn.didwebauthn.ui.didcomm.addcontacts.AddContactRouter
import dagger.Module
import dagger.Provides

/**
 * AddContact VIPER Dagger Module
 */
@Module
class AddContactModule: BaseFragmentModule<AddContactFragment>() {

    @Provides
    fun providePresenter(impl: AddContactPresenter): AddContactContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: AddContactInteractor): AddContactContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: AddContactRouter): AddContactContract.Router = impl

    @Provides
    @DidInformation
    fun provideDidInfo(fragment: AddContactFragment) = fragment.navigationArgs.didInfo
}
