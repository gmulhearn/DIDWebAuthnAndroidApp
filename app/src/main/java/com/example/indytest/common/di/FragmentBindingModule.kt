package com.example.indytest.common.di

import com.example.indytest.DIDs.DIDsFragment
import com.example.indytest.DIDs.DIDsModule
import com.example.indytest.Wallets.IDGenerationFragment
import com.example.indytest.Wallets.IDGenerationModule
import com.example.indytest.Signing.SigningFragment
import com.example.indytest.Signing.SigningModule
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
