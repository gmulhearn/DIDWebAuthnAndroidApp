package com.example.indytest.base.di

import com.example.indytest.IDGeneration.IDGenerationFragment
import com.example.indytest.IDGeneration.IDGenerationModule
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
    @ContributesAndroidInjector(modules = [SigningModule::class])
    abstract fun bindSigningFragment(): SigningFragment
}
