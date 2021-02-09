package com.example.did.common.di

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import javax.inject.Scope

@Scope
annotation class ActivityScope

@Scope
annotation class FragmentScope

@Qualifier
annotation class ViewContext

@Module
abstract class BaseActivityModule<T : AppCompatActivity> {

    @Provides
    fun provideActivity(activity: T): Activity = activity

    @Provides
    fun provideAppCompatActivity(activity: T): AppCompatActivity = activity

    @ViewContext
    @Provides
    fun provideViewContext(activity: T): Context = activity

    @Provides
    fun provideResources(activity: Activity): Resources = activity.resources
}

@Module
abstract class BaseFragmentModule<T : Fragment> {

    @Provides
    fun fragmentActivity(fragment: T): androidx.fragment.app.FragmentActivity = fragment.requireActivity()

    @Provides
    fun activity(activity: androidx.fragment.app.FragmentActivity): Activity = activity

    @ViewContext
    @Provides
    fun provideViewContext(fragment: T): Context = fragment.requireContext()

    @Provides
    fun provideResources(fragment: T): Resources = fragment.resources
}
