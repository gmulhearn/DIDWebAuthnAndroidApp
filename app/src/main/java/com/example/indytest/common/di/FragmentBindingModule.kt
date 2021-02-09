package com.example.indytest.common.di

import com.example.indytest.ui.dids.DIDsFragment
import com.example.indytest.ui.dids.DIDsModule
import com.example.indytest.ui.wallets.IDGenerationFragment
import com.example.indytest.ui.wallets.IDGenerationModule
import com.example.indytest.ui.signing.SigningFragment
import com.example.indytest.ui.signing.SigningModule
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
