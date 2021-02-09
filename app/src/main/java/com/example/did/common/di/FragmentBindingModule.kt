package com.example.did.common.di

import com.example.did.ui.dids.DIDsFragment
import com.example.did.ui.dids.DIDsModule
import com.example.did.ui.wallets.IDGenerationFragment
import com.example.did.ui.wallets.IDGenerationModule
import com.example.did.ui.signing.SigningFragment
import com.example.did.ui.signing.SigningModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [IDGenerationModule::class])
    abstract fun bindGenerationFragment(): IDGenerationFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [DIDsModule::class])
    abstract fun bindDIDsFragment(): DIDsFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [SigningModule::class])
    abstract fun bindSigningFragment(): SigningFragment
}
