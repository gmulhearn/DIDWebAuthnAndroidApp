package com.example.indytest.common.di

import android.app.Application
import android.content.Context
import com.example.indytest.common.DefaultDispatcherProvider
import com.example.indytest.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        FragmentBindingModule::class
    ]
)
class AppModule {

    @Provides
    @Singleton
    fun provideApplication(context: Context): Application {
        return context.applicationContext as Application
    }

    @Provides
    @Singleton
    fun provideDispatchersProvider(impl: DefaultDispatcherProvider): DispatcherProvider = impl
}
