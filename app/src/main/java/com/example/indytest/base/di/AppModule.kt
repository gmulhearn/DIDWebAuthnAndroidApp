package com.example.indytest.base.di

import android.app.Application
import android.content.Context
import com.example.indytest.base.DefaultDispatcherProvider
import com.example.indytest.base.DispatcherProvider
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
