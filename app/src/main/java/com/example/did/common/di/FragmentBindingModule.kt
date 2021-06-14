package com.example.did.common.di

import com.example.did.ui.browser.BrowserFragment
import com.example.did.ui.browser.BrowserModule
import com.example.did.ui.didcomm.AddContact.AddContactFragment
import com.example.did.ui.didcomm.AddContact.AddContactModule
import com.example.did.ui.didcomm.chat.ChatFragment
import com.example.did.ui.didcomm.chat.ChatModule
import com.example.did.ui.didcomm.contactselect.ContactSelectFragment
import com.example.did.ui.didcomm.contactselect.ContactSelectModule
import com.example.did.ui.didselect.DIDSelectFragment
import com.example.did.ui.didselect.DIDSelectModule
import com.example.did.ui.externalauth.ExternalAuthFragment
import com.example.did.ui.externalauth.ExternalAuthModule
import com.example.did.ui.externalsession.ExternalSessionFragment
import com.example.did.ui.externalsession.ExternalSessionModule
import com.example.did.ui.wallets.WalletsFragment
import com.example.did.ui.wallets.WalletsModule
import com.example.did.ui.signing.SigningFragment
import com.example.did.ui.signing.SigningModule
import com.example.did.ui.walletInfo.WalletInfoFragment
import com.example.did.ui.walletInfo.WalletInfoModule
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
