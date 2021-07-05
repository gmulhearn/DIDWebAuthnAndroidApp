package com.gmulhearn.didwebauthn.ui.didcomm.chat

import com.gmulhearn.didwebauthn.common.di.BaseFragmentModule
import dagger.Module
import dagger.Provides

/**
 * Chat VIPER Dagger Module
 */
@Module
class ChatModule: BaseFragmentModule<ChatFragment>() {

    @Provides
    fun providePresenter(impl: ChatPresenter): ChatContract.Presenter = impl

    @Provides
    fun provideInteractor(impl: ChatInteractor): ChatContract.InteractorInput = impl

    @Provides
    fun provideRouter(impl: ChatRouter): ChatContract.Router = impl

    @Provides
    fun providePairwiseContact(fragment: ChatFragment) = fragment.navigationArgs.pairwiseContact
}
