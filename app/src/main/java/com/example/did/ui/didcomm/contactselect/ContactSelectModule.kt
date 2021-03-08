package com.example.did.ui.didcomm.contactselect

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

    @Provides
    @DidInformation
    fun provideDidInfo(fragment: ContactSelectFragment) = fragment.navigationArgs.didInfo
}
