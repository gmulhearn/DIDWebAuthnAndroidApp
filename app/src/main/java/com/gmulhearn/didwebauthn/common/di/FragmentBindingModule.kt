package com.gmulhearn.didwebauthn.common.di

import com.gmulhearn.didwebauthn.ui.browser.BrowserFragment
import com.gmulhearn.didwebauthn.ui.browser.BrowserModule
import com.gmulhearn.didwebauthn.ui.didcomm.addcontacts.AddContactFragment
import com.gmulhearn.didwebauthn.ui.didcomm.AddContact.AddContactModule
import com.gmulhearn.didwebauthn.ui.didcomm.chat.ChatFragment
import com.gmulhearn.didwebauthn.ui.didcomm.chat.ChatModule
import com.gmulhearn.didwebauthn.ui.didcomm.contactselect.ContactSelectFragment
import com.gmulhearn.didwebauthn.ui.didcomm.contactselect.ContactSelectModule
import com.gmulhearn.didwebauthn.ui.didselect.DIDSelectFragment
import com.gmulhearn.didwebauthn.ui.didselect.DIDSelectModule
import com.gmulhearn.didwebauthn.ui.externalauth.ExternalAuthFragment
import com.gmulhearn.didwebauthn.ui.externalauth.ExternalAuthModule
import com.gmulhearn.didwebauthn.ui.externalsession.ExternalSessionFragment
import com.gmulhearn.didwebauthn.ui.externalsession.ExternalSessionModule
import com.gmulhearn.didwebauthn.ui.wallets.WalletsFragment
import com.gmulhearn.didwebauthn.ui.wallets.WalletsModule
import com.gmulhearn.didwebauthn.ui.signing.SigningFragment
import com.gmulhearn.didwebauthn.ui.signing.SigningModule
import com.gmulhearn.didwebauthn.ui.walletInfo.WalletInfoFragment
import com.gmulhearn.didwebauthn.ui.walletInfo.WalletInfoModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [WalletsModule::class])
    abstract fun bindGenerationFragment(): WalletsFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [DIDSelectModule::class])
    abstract fun bindDIDSelectFragment(): DIDSelectFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [SigningModule::class])
    abstract fun bindSigningFragment(): SigningFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactSelectModule::class])
    abstract fun bindContactSelectFragment(): ContactSelectFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [AddContactModule::class])
    abstract fun bindAddContactFragment(): AddContactFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ChatModule::class])
    abstract fun bindChatFragment(): ChatFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [BrowserModule::class])
    abstract fun bindBrowserFragment(): BrowserFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [WalletInfoModule::class])
    abstract fun bindWalletInfoFragment(): WalletInfoFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ExternalAuthModule::class])
    abstract fun bindExternalAuthFragment(): ExternalAuthFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [ExternalSessionModule::class])
    abstract fun bindExternalSessionFragment(): ExternalSessionFragment
}
