package com.example.did.ui.didcomm.AddContact

import com.example.did.common.di.BaseFragmentModule
import com.example.did.common.di.qualifier.DidInformation
import com.example.did.common.di.qualifier.WalletInformation
import com.example.did.ui.didselect.DIDSelectContract
import com.example.did.ui.didselect.DIDSelectFragment
import com.example.did.ui.didselect.DIDSelectRouter
import com.example.did.ui.signing.SigningFragment
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
