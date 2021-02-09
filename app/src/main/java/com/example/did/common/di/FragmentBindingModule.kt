package com.example.did.common.di

import com.example.did.ui.dids.DIDsFragment
import com.example.did.ui.dids.DIDsModule
import com.example.did.ui.wallets.WalletsFragment
import com.example.did.ui.wallets.WalletsModule
import com.example.did.ui.signing.SigningFragment
import com.example.did.ui.signing.SigningModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [WalletsModule::class])
    abstract fun bindGenerationFragment(): WalletsFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [DIDsModule::class])
    abstract fun bindDIDsFragment(): DIDsFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [SigningModule::class])
    abstract fun bindSigningFragment(): SigningFragment
}
